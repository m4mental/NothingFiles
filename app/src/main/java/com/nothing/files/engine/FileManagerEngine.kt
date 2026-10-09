package com.nothing.files.engine

import android.os.Environment
import com.nothing.files.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

import java.util.zip.ZipFile

enum class SortBy {
    NAME,
    DATE,
    SIZE,
    TYPE
}

enum class SortOption(val title: String) {
    NAME_ASC("Name (A - Z)"),
    NAME_DESC("Name (Z - A)"),
    SIZE_ASC("Size (Smaller)"),
    SIZE_DESC("Size (Larger)"),
    DATE_ASC("Date (Older)"),
    DATE_DESC("Date (Newer)"),
    TYPE_ASC("Type (Ascending)"),
    TYPE_DESC("Type (Descending)"),
    SHUFFLED("Shuffled")
}

class FileManagerEngine {

    val defaultStoragePath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    fun findArchiveInfo(path: String): Pair<File, String>? {
        var current: File? = File(path)
        val subParts = mutableListOf<String>()
        while (current != null) {
            if (current.exists() && current.isFile) {
                val ext = current.extension.lowercase()
                if (ext in listOf("zip", "apk", "jar")) {
                    val subPath = subParts.asReversed().joinToString("/")
                    return Pair(current, subPath)
                }
            }
            subParts.add(current.name)
            current = current.parentFile
        }
        return null
    }

    private fun listArchiveItems(archiveFile: File, subPath: String): List<FileItem> {
        val items = mutableListOf<FileItem>()
        try {
            ZipFile(archiveFile).use { zip ->
                val normalizedSubPath = if (subPath.isBlank()) "" else subPath.trim('/') + "/"
                val directDirs = mutableSetOf<String>()
                val enumEntries = zip.entries()
                while (enumEntries.hasMoreElements()) {
                    val entry = enumEntries.nextElement()
                    val entryName = entry.name
                    if (normalizedSubPath.isNotEmpty() && !entryName.startsWith(normalizedSubPath)) {
                        continue
                    }
                    val relative = entryName.removePrefix(normalizedSubPath).removeSuffix("/")
                    if (relative.isEmpty()) continue

                    val slashIndex = relative.indexOf('/')
                    if (slashIndex != -1) {
                        val dirName = relative.substring(0, slashIndex)
                        if (directDirs.add(dirName)) {
                            val fullPath = "${archiveFile.absolutePath}/${normalizedSubPath}$dirName"
                            items.add(
                                FileItem(
                                    name = dirName,
                                    path = fullPath,
                                    isDirectory = true,
                                    size = 0L,
                                    lastModified = entry.time,
                                    permissions = "drwxr-xr-x"
                                )
                            )
                        }
                    } else if (entry.isDirectory) {
                        if (directDirs.add(relative)) {
                            val fullPath = "${archiveFile.absolutePath}/${normalizedSubPath}$relative"
                            items.add(
                                FileItem(
                                    name = relative,
                                    path = fullPath,
                                    isDirectory = true,
                                    size = 0L,
                                    lastModified = entry.time,
                                    permissions = "drwxr-xr-x"
                                )
                            )
                        }
                    } else {
                        val fullPath = "${archiveFile.absolutePath}/${normalizedSubPath}$relative"
                        items.add(
                            FileItem(
                                name = relative,
                                path = fullPath,
                                isDirectory = false,
                                size = entry.size.coerceAtLeast(0),
                                lastModified = entry.time,
                                permissions = "-rw-r--r--"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    suspend fun listFiles(
        directoryPath: String,
        sortBy: SortBy = SortBy.NAME,
        ascending: Boolean = true,
        showHidden: Boolean = false
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val archiveInfo = findArchiveInfo(directoryPath)
        val items: List<FileItem> = if (archiveInfo != null) {
            listArchiveItems(archiveInfo.first, archiveInfo.second)
        } else {
            val dir = File(directoryPath)
            val isProtected = PrivilegedFileEngine.isProtectedPath(directoryPath)
            if (isProtected || !dir.exists() || !dir.isDirectory) {
                PrivilegedFileEngine.listFiles(directoryPath, showHidden)
            } else {
                val rawFiles = dir.listFiles()
                if (rawFiles != null && rawFiles.isNotEmpty()) {
                    rawFiles
                        .filter { showHidden || !it.name.startsWith(".") }
                        .map { FileItem.fromFile(it) }
                } else {
                    PrivilegedFileEngine.listFiles(directoryPath, showHidden)
                }
            }
        }

        // Folders always first, then sort by selected criteria
        val comparator = when (sortBy) {
            SortBy.NAME -> compareBy<FileItem> { it.name.lowercase() }
            SortBy.DATE -> compareBy<FileItem> { it.lastModified }
            SortBy.SIZE -> compareBy<FileItem> { it.size }
            SortBy.TYPE -> compareBy<FileItem> { it.extension }
        }

        val sorted = if (ascending) items.sortedWith(comparator) else items.sortedWith(comparator.reversed())
        // Keep directories on top
        sorted.sortedByDescending { it.isDirectory }
    }

    suspend fun listFiles(
        directoryPath: String,
        sortOption: SortOption,
        showHidden: Boolean = false,
        foldersFirst: Boolean = true
    ): List<FileItem> = withContext(Dispatchers.IO) {
        val archiveInfo = findArchiveInfo(directoryPath)
        val items: List<FileItem> = if (archiveInfo != null) {
            listArchiveItems(archiveInfo.first, archiveInfo.second)
        } else {
            val dir = File(directoryPath)
            val isProtected = PrivilegedFileEngine.isProtectedPath(directoryPath)
            val rawFiles = if (!isProtected && dir.exists() && dir.isDirectory) dir.listFiles() else null
            if (rawFiles != null && rawFiles.isNotEmpty()) {
                rawFiles
                    .filter { showHidden || !it.name.startsWith(".") }
                    .map { FileItem.fromFile(it) }
            } else {
                PrivilegedFileEngine.listFiles(directoryPath, showHidden)
            }
        }

        if (sortOption == SortOption.SHUFFLED) {
            val shuffled = items.shuffled()
            return@withContext if (foldersFirst) shuffled.sortedByDescending { it.isDirectory } else shuffled
        }

        val comparator = when (sortOption) {
            SortOption.NAME_ASC -> compareBy<FileItem> { it.name.lowercase() }
            SortOption.NAME_DESC -> compareByDescending<FileItem> { it.name.lowercase() }
            SortOption.SIZE_ASC -> compareBy<FileItem> { it.size }
            SortOption.SIZE_DESC -> compareByDescending<FileItem> { it.size }
            SortOption.DATE_ASC -> compareBy<FileItem> { it.lastModified }
            SortOption.DATE_DESC -> compareByDescending<FileItem> { it.lastModified }
            SortOption.TYPE_ASC -> compareBy<FileItem> { it.extension.lowercase() }.thenBy { it.name.lowercase() }
            SortOption.TYPE_DESC -> compareByDescending<FileItem> { it.extension.lowercase() }.thenBy { it.name.lowercase() }
            SortOption.SHUFFLED -> compareBy<FileItem> { it.name.lowercase() }
        }

        val sorted = items.sortedWith(comparator)
        if (foldersFirst) {
            sorted.sortedByDescending { it.isDirectory }
        } else {
            sorted
        }
    }

    suspend fun copyFiles(sources: List<File>, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        if (!destinationDir.exists()) {
            val made = destinationDir.mkdirs()
            if (!made) {
                PrivilegedFileEngine.executePrivileged("mkdir -p \"${destinationDir.absolutePath}\"")
            }
        }
        var success = true
        for (src in sources) {
            try {
                if (src.isDirectory) {
                    val destSubDir = File(destinationDir, src.name)
                    destSubDir.mkdirs()
                    src.copyRecursively(destSubDir, overwrite = true)
                } else {
                    val destFile = File(destinationDir, src.name)
                    src.copyTo(destFile, overwrite = true)
                }
            } catch (e: Exception) {
                // Fallback to privileged cp (Root or Shizuku)
                val res = PrivilegedFileEngine.executePrivileged("cp -rf \"${src.absolutePath}\" \"${destinationDir.absolutePath}/\"")
                if (res.exitCode != 0) {
                    e.printStackTrace()
                    success = false
                }
            }
        }
        success
    }

    suspend fun moveFiles(sources: List<File>, destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        if (!destinationDir.exists()) {
            val made = destinationDir.mkdirs()
            if (!made) {
                PrivilegedFileEngine.executePrivileged("mkdir -p \"${destinationDir.absolutePath}\"")
            }
        }
        var success = true
        for (src in sources) {
            try {
                val destFile = File(destinationDir, src.name)
                val moved = src.renameTo(destFile)
                if (!moved) {
                    val res = PrivilegedFileEngine.executePrivileged("mv -f \"${src.absolutePath}\" \"${destinationDir.absolutePath}/\"")
                    if (res.exitCode != 0) {
                        if (src.isDirectory) {
                            src.copyRecursively(destFile, overwrite = true)
                            src.deleteRecursively()
                        } else {
                            src.copyTo(destFile, overwrite = true)
                            src.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                val res = PrivilegedFileEngine.executePrivileged("mv -f \"${src.absolutePath}\" \"${destinationDir.absolutePath}/\"")
                if (res.exitCode != 0) {
                    e.printStackTrace()
                    success = false
                }
            }
        }
        success
    }

    suspend fun deleteFiles(files: List<File>): Boolean = withContext(Dispatchers.IO) {
        var success = true
        for (f in files) {
            try {
                val deleted = if (f.isDirectory) f.deleteRecursively() else f.delete()
                if (!deleted) {
                    val res = PrivilegedFileEngine.executePrivileged("rm -rf \"${f.absolutePath}\"")
                    if (res.exitCode != 0) success = false
                }
            } catch (e: Exception) {
                val res = PrivilegedFileEngine.executePrivileged("rm -rf \"${f.absolutePath}\"")
                if (res.exitCode != 0) {
                    e.printStackTrace()
                    success = false
                }
            }
        }
        success
    }

    suspend fun renameFile(target: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val destination = File(target.parentFile, newName)
            val renamed = target.renameTo(destination)
            if (!renamed) {
                val res = PrivilegedFileEngine.executePrivileged("mv -f \"${target.absolutePath}\" \"${destination.absolutePath}\"")
                res.exitCode == 0
            } else true
        } catch (e: Exception) {
            val destination = File(target.parentFile, newName)
            val res = PrivilegedFileEngine.executePrivileged("mv -f \"${target.absolutePath}\" \"${destination.absolutePath}\"")
            res.exitCode == 0
        }
    }

    suspend fun batchRename(
        files: List<File>,
        prefix: String = "",
        suffix: String = "",
        replaceFind: String = "",
        replaceWith: String = "",
        includeIndex: Boolean = false,
        startIndex: Int = 1
    ): Int = withContext(Dispatchers.IO) {
        var renamedCount = 0
        files.forEachIndexed { i, file ->
            val ext = if (file.isDirectory) "" else "." + file.extension
            val baseName = if (file.isDirectory) file.name else file.nameWithoutExtension

            var newBase = baseName
            if (replaceFind.isNotEmpty()) {
                newBase = newBase.replace(replaceFind, replaceWith)
            }

            val indexPart = if (includeIndex) "_${startIndex + i}" else ""
            val finalName = "$prefix$newBase$indexPart$suffix$ext"

            val target = File(file.parentFile, finalName)
            if (file.renameTo(target)) {
                renamedCount++
            } else {
                val res = PrivilegedFileEngine.executePrivileged("mv -f \"${file.absolutePath}\" \"${target.absolutePath}\"")
                if (res.exitCode == 0) renamedCount++
            }
        }
        renamedCount
    }

    suspend fun createFolder(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val folder = File(parent, name)
        val created = folder.mkdirs()
        if (!created) {
            val res = PrivilegedFileEngine.executePrivileged("mkdir -p \"${folder.absolutePath}\"")
            res.exitCode == 0
        } else true
    }

    suspend fun createFile(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(parent, name)
        try {
            val created = file.createNewFile()
            if (!created) {
                val res = PrivilegedFileEngine.executePrivileged("touch \"${file.absolutePath}\"")
                res.exitCode == 0
            } else true
        } catch (e: Exception) {
            val res = PrivilegedFileEngine.executePrivileged("touch \"${file.absolutePath}\"")
            res.exitCode == 0
        }
    }

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/system/usr/we-need-root/su-backup",
            "/system/xbin/mu"
        )
        paths.any { File(it).exists() } || try {
            val process = Runtime.getRuntime().exec("su -c id")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (_: Exception) {
            false
        }
    }
}
