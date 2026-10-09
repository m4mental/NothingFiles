package com.nothing.files.engine

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64
import com.nothing.files.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

object PrivilegedFileEngine {

    const val SHIZUKU_REQUEST_CODE = 1001

    enum class PrivilegeMode(val id: String, val title: String, val subtitle: String) {
        AUTO("auto", "Auto Privileged", "Automatically selects Root SU, Shizuku, or Normal as needed"),
        ROOT("root", "Root Privileged", "Full UID 0 Superuser access for Root directory & all files"),
        SHIZUKU("shizuku", "Shizuku Privileged", "Elevated ADB UID 2000 access for Android/data, obb & packages"),
        NORMAL("normal", "Normal Privileged", "Standard Android file access without privileged elevation")
    }

    private var cachedMode: PrivilegeMode? = null

    fun getPrivilegeMode(context: Context): PrivilegeMode {
        cachedMode?.let { return it }
        val prefs = context.getSharedPreferences("nothing_files_privilege", Context.MODE_PRIVATE)
        val id = prefs.getString("privilege_mode", PrivilegeMode.AUTO.id) ?: PrivilegeMode.AUTO.id
        val mode = PrivilegeMode.values().firstOrNull { it.id == id } ?: PrivilegeMode.AUTO
        cachedMode = mode
        return mode
    }

    fun setPrivilegeMode(context: Context, mode: PrivilegeMode) {
        cachedMode = mode
        val prefs = context.getSharedPreferences("nothing_files_privilege", Context.MODE_PRIVATE)
        prefs.edit().putString("privilege_mode", mode.id).apply()
    }

    suspend fun getActivePrivilegeTitle(context: Context): String = withContext(Dispatchers.IO) {
        val mode = getPrivilegeMode(context)
        when (mode) {
            PrivilegeMode.ROOT -> if (isRootAvailable()) "Root Privileged (UID 0)" else "Root (Unavailable)"
            PrivilegeMode.SHIZUKU -> if (isShizukuPermissionGranted()) "Shizuku Privileged (UID 2000)" else "Shizuku (Permission Required)"
            PrivilegeMode.NORMAL -> "Normal Privileged"
            PrivilegeMode.AUTO -> {
                if (isRootAvailable()) "Root Privileged (UID 0)"
                else if (isShizukuPermissionGranted()) "Shizuku Privileged (UID 2000)"
                else "Normal Privileged"
            }
        }
    }

    // ==========================================
    // 1. Root & Shizuku Status Checks
    // ==========================================

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        RootShellEngine.isRootAvailable()
    }

    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (_: Exception) {
            try {
                context.packageManager.getPackageInfo("rikka.shizuku", 0)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun isShizukuRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun isShizukuPermissionGranted(): Boolean {
        return try {
            if (!isShizukuRunning()) false
            else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun requestShizukuPermission() {
        try {
            if (isShizukuRunning() && !isShizukuPermissionGranted()) {
                Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
            }
        } catch (_: Throwable) {}
    }

    fun isProtectedPath(path: String): Boolean {
        val lower = path.lowercase()
        return lower.contains("/android/data") ||
                lower.contains("/android/obb") ||
                path.startsWith("/system") ||
                path.startsWith("/data") ||
                path.startsWith("/vendor") ||
                path.startsWith("/apex") ||
                path.startsWith("/root") ||
                path.startsWith("/etc") ||
                path == "/"
    }

    // ==========================================
    // 2. Privileged Command Execution
    // ==========================================

    suspend fun executePrivileged(command: String, context: Context? = null): ShellResult = withContext(Dispatchers.IO) {
        val mode = context?.let { getPrivilegeMode(it) } ?: cachedMode ?: PrivilegeMode.AUTO

        when (mode) {
            PrivilegeMode.ROOT -> {
                if (RootShellEngine.isRootAvailable()) {
                    return@withContext RootShellEngine.executeCommand(command, runAsRoot = true)
                }
            }
            PrivilegeMode.SHIZUKU -> {
                if (isShizukuPermissionGranted()) {
                    return@withContext executeShizukuCommand(command)
                }
            }
            PrivilegeMode.NORMAL -> {
                return@withContext RootShellEngine.executeCommand(command, runAsRoot = false)
            }
            PrivilegeMode.AUTO -> {
                // Priority 1: Root SU if available
                if (RootShellEngine.isRootAvailable()) {
                    return@withContext RootShellEngine.executeCommand(command, runAsRoot = true)
                }
                // Priority 2: Shizuku if granted
                if (isShizukuPermissionGranted()) {
                    return@withContext executeShizukuCommand(command)
                }
            }
        }

        // Fallback standard shell
        RootShellEngine.executeCommand(command, runAsRoot = false)
    }

    private fun executeShizukuCommand(command: String): ShellResult {
        return try {
            val shizukuClass = Class.forName("rikka.shizuku.Shizuku")
            val newProcessMethod = shizukuClass.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            val process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as Process
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout.trim(), stderr.trim(), false)
        } catch (e: Throwable) {
            ShellResult(-1, "", e.message ?: "Shizuku execution failed", false)
        }
    }

    // ==========================================
    // 3. Directory Listing via Privileged Shell
    // ==========================================

    suspend fun listFiles(directoryPath: String, showHidden: Boolean = false): List<FileItem> = withContext(Dispatchers.IO) {
        val cleanDir = if (directoryPath.endsWith("/")) directoryPath.dropLast(1) else directoryPath
        val safePath = if (cleanDir.isEmpty()) "/" else cleanDir

        // 1. Try standard query
        var res = executePrivileged("ls -la \"$safePath\"")
        var items = parseLsOutput(res.stdout, safePath, showHidden)

        // 2. Physical raw storage path fallback for KernelSU / Magisk mount namespace separation
        if (items.isEmpty() && safePath.startsWith("/storage/emulated/0/Android", ignoreCase = true)) {
            val altPath = safePath.replaceFirst("/storage/emulated/0", "/data/media/0")
            val altRes = executePrivileged("ls -la \"$altPath\"")
            if (altRes.exitCode == 0 && altRes.stdout.isNotBlank()) {
                val altItems = parseLsOutput(altRes.stdout, altPath, showHidden)
                if (altItems.isNotEmpty()) {
                    return@withContext altItems.map { it.copy(path = it.path.replaceFirst("/data/media/0", "/storage/emulated/0")) }
                }
            }
        }

        items
    }

    private fun parseLsOutput(stdout: String, targetDir: String, showHidden: Boolean): List<FileItem> {
        if (stdout.isBlank()) return emptyList()
        val items = mutableListOf<FileItem>()
        for (rawLine in stdout.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("total")) continue

            val tokens = line.split("\\s+".toRegex())
            if (tokens.size < 7) continue

            val perms = tokens[0]
            if (!perms.matches("^[dcb-lps][rwxstST-]{9}.*".toRegex())) continue

            val isDirectory = perms.startsWith("d")
            val isSymlink = perms.startsWith("l")

            // Determine size
            val size = tokens.getOrNull(4)?.toLongOrNull() ?: 0L

            val nameRaw = if (tokens.size >= 8) tokens.drop(7).joinToString(" ") else tokens.last()
            val name = if (isSymlink && nameRaw.contains(" -> ")) {
                nameRaw.substringBefore(" -> ").trim()
            } else {
                nameRaw.trim()
            }

            if (name == "." || name == "..") continue
            if (!showHidden && name.startsWith(".")) continue

            val fullPath = if (targetDir == "/") "/$name" else "$targetDir/$name"

            items.add(
                FileItem(
                    name = name,
                    path = fullPath,
                    isDirectory = isDirectory,
                    size = size,
                    lastModified = System.currentTimeMillis(),
                    isHidden = name.startsWith("."),
                    isSymlink = isSymlink,
                    permissions = perms.take(10),
                    childCount = 0
                )
            )
        }
        return items
    }

    // ==========================================
    // 4. File Read & Write via Privileged Shell
    // ==========================================

    suspend fun readText(path: String): String? = withContext(Dispatchers.IO) {
        try {
            // Standard read first
            val f = File(path)
            if (f.canRead()) {
                return@withContext f.readText(Charsets.UTF_8)
            }
        } catch (_: Exception) {}

        // Privileged read via base64 encoded stream
        val cmd = "base64 \"$path\""
        val res = executePrivileged(cmd)
        if (res.exitCode == 0 && res.stdout.isNotBlank()) {
            try {
                val decoded = Base64.decode(res.stdout, Base64.DEFAULT)
                return@withContext String(decoded, Charsets.UTF_8)
            } catch (_: Exception) {}
        }

        // Direct cat fallback
        val catRes = executePrivileged("cat \"$path\"")
        if (catRes.exitCode == 0) {
            catRes.stdout
        } else {
            null
        }
    }

    suspend fun writeText(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val f = File(path)
            if (f.exists() && f.canWrite()) {
                f.writeText(content, Charsets.UTF_8)
                return@withContext true
            }
        } catch (_: Exception) {}

        // Privileged write using base64 decoding (100% shell safe)
        val b64 = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val cmd = "echo \"$b64\" | base64 -d > \"$path\""
        val res = executePrivileged(cmd)
        if (res.exitCode == 0) {
            return@withContext true
        }

        // Fallback using printf
        val directRes = executePrivileged("sh -c \"cat > '$path'\" << 'EOF_NOTHING_FILES'\n$content\nEOF_NOTHING_FILES")
        directRes.exitCode == 0
    }

    // ==========================================
    // 5. File Operations via Privileged Shell
    // ==========================================

    suspend fun copy(srcPath: String, dstDir: String): Boolean = withContext(Dispatchers.IO) {
        var res = executePrivileged("cp -rf \"$srcPath\" \"$dstDir/\"")
        if (res.exitCode == 0) return@withContext true
        if (dstDir.startsWith("/storage/emulated/0/Android", ignoreCase = true) || srcPath.startsWith("/storage/emulated/0/Android", ignoreCase = true)) {
            val altDst = dstDir.replaceFirst("/storage/emulated/0", "/data/media/0")
            val altSrc = srcPath.replaceFirst("/storage/emulated/0", "/data/media/0")
            res = executePrivileged("cp -rf \"$altSrc\" \"$altDst/\"")
        }
        res.exitCode == 0
    }

    suspend fun move(srcPath: String, dstDir: String): Boolean = withContext(Dispatchers.IO) {
        var res = executePrivileged("mv -f \"$srcPath\" \"$dstDir/\"")
        if (res.exitCode == 0) return@withContext true
        if (dstDir.startsWith("/storage/emulated/0/Android", ignoreCase = true) || srcPath.startsWith("/storage/emulated/0/Android", ignoreCase = true)) {
            val altDst = dstDir.replaceFirst("/storage/emulated/0", "/data/media/0")
            val altSrc = srcPath.replaceFirst("/storage/emulated/0", "/data/media/0")
            res = executePrivileged("mv -f \"$altSrc\" \"$altDst/\"")
        }
        res.exitCode == 0
    }

    suspend fun delete(path: String): Boolean = withContext(Dispatchers.IO) {
        var res = executePrivileged("rm -rf \"$path\"")
        if (res.exitCode == 0) return@withContext true
        if (path.startsWith("/storage/emulated/0/Android", ignoreCase = true)) {
            val altPath = path.replaceFirst("/storage/emulated/0", "/data/media/0")
            res = executePrivileged("rm -rf \"$altPath\"")
        }
        res.exitCode == 0
    }

    suspend fun mkdir(path: String): Boolean = withContext(Dispatchers.IO) {
        var res = executePrivileged("mkdir -p \"$path\"")
        if (res.exitCode == 0) return@withContext true
        if (path.startsWith("/storage/emulated/0/Android", ignoreCase = true)) {
            val altPath = path.replaceFirst("/storage/emulated/0", "/data/media/0")
            res = executePrivileged("mkdir -p \"$altPath\"")
        }
        res.exitCode == 0
    }

    suspend fun createFile(path: String): Boolean = withContext(Dispatchers.IO) {
        var res = executePrivileged("touch \"$path\"")
        if (res.exitCode == 0) return@withContext true
        if (path.startsWith("/storage/emulated/0/Android", ignoreCase = true)) {
            val altPath = path.replaceFirst("/storage/emulated/0", "/data/media/0")
            res = executePrivileged("touch \"$altPath\"")
        }
        res.exitCode == 0
    }

    suspend fun chmod(path: String, mode: String): Boolean = withContext(Dispatchers.IO) {
        val res = executePrivileged("chmod $mode \"$path\"")
        res.exitCode == 0
    }
}
