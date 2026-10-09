package com.nothing.files.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class ArchiveEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val uncompressedSize: Long,
    val compressedSize: Long,
    val time: Long
)

class ArchiveEngine {

    suspend fun listZipEntries(zipFile: File): List<ArchiveEntry> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<ArchiveEntry>()
        try {
            ZipFile(zipFile).use { zip ->
                val enumEntries = zip.entries()
                while (enumEntries.hasMoreElements()) {
                    val entry = enumEntries.nextElement()
                    val cleanPath = entry.name.removeSuffix("/")
                    val displayName = cleanPath.substringAfterLast('/')
                    entries.add(
                        ArchiveEntry(
                            name = displayName,
                            path = entry.name,
                            isDirectory = entry.isDirectory,
                            uncompressedSize = entry.size.coerceAtLeast(0),
                            compressedSize = entry.compressedSize.coerceAtLeast(0),
                            time = entry.time
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        entries.sortedWith(compareByDescending<ArchiveEntry> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    suspend fun extractZip(
        zipFile: File,
        targetDir: File,
        onProgress: (extractedCount: Int, totalCount: Int, currentFile: String) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetDirPath = targetDir.canonicalPath + File.separator
        try {
            ZipFile(zipFile).use { zip ->
                val total = zip.size()
                var current = 0
                val entries = zip.entries()

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val outFile = File(targetDir, entry.name)

                    // Prevent Zip Slip vulnerability
                    val outCanonical = outFile.canonicalPath
                    if (!outCanonical.startsWith(targetDirPath) && outCanonical != targetDir.canonicalPath) {
                        continue
                    }

                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            BufferedOutputStream(FileOutputStream(outFile)).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    current++
                    onProgress(current, total, entry.name)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            // If standard Java extraction failed (e.g. root/system folder permissions), fallback to shell unzip if available
            try {
                if (RootShellEngine.isRootAvailable()) {
                    val escapedZip = zipFile.absolutePath.replace("\"", "\\\"")
                    val escapedTarget = targetDir.absolutePath.replace("\"", "\\\"")
                    val res = RootShellEngine.executeCommand("mkdir -p \"$escapedTarget\" && unzip -o \"$escapedZip\" -d \"$escapedTarget\"", runAsRoot = true)
                    res.exitCode == 0
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun extractArchive(
        archiveFile: File,
        targetDir: File,
        onProgress: (extractedCount: Int, totalCount: Int, currentFile: String) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        val name = archiveFile.name.lowercase()
        if (name.endsWith(".tar") || name.endsWith(".tar.gz") || name.endsWith(".tgz") || name.endsWith(".tar.bz2") || name.endsWith(".tar.xz")) {
            extractTar(archiveFile, targetDir)
        } else {
            extractZip(archiveFile, targetDir, onProgress)
        }
    }

    private suspend fun extractTar(archiveFile: File, targetDir: File): Boolean = withContext(Dispatchers.IO) {
        if (!targetDir.exists()) targetDir.mkdirs()
        val escapedArchive = archiveFile.absolutePath.replace("\"", "\\\"")
        val escapedTarget = targetDir.absolutePath.replace("\"", "\\\"")
        val isRoot = RootShellEngine.isRootAvailable()
        val res = RootShellEngine.executeCommand("tar -xf \"$escapedArchive\" -C \"$escapedTarget\"", runAsRoot = isRoot)
        if (res.exitCode == 0) return@withContext true
        val resToybox = RootShellEngine.executeCommand("toybox tar -xf \"$escapedArchive\" -C \"$escapedTarget\"", runAsRoot = isRoot)
        resToybox.exitCode == 0
    }

    suspend fun createZip(
        sourceFiles: List<File>,
        outputZipFile: File,
        onProgress: (current: Int, total: Int, currentFile: String) -> Unit = { _, _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val allFiles = mutableListOf<File>()
            for (f in sourceFiles) {
                if (f.isDirectory) {
                    allFiles.addAll(f.walkTopDown().filter { it.isFile })
                } else {
                    allFiles.add(f)
                }
            }

            ZipOutputStream(BufferedOutputStream(FileOutputStream(outputZipFile))).use { zos ->
                val baseDir = sourceFiles.firstOrNull()?.parentFile ?: outputZipFile.parentFile
                val basePath = baseDir?.canonicalPath ?: ""

                allFiles.forEachIndexed { index, file ->
                    val relativePath = file.canonicalPath.removePrefix(basePath).removePrefix(File.separator)
                    val entry = ZipEntry(relativePath.replace('\\', '/'))
                    entry.time = file.lastModified()
                    zos.putNextEntry(entry)

                    BufferedInputStream(FileInputStream(file)).use { bis ->
                        bis.copyTo(zos)
                    }
                    zos.closeEntry()
                    onProgress(index + 1, allFiles.size, file.name)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun extractSingleEntry(
        zipFile: File,
        entryPath: String,
        outFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            ZipFile(zipFile).use { zip ->
                val cleanPath = entryPath.trimStart('/')
                val entry = zip.getEntry(cleanPath)
                    ?: zip.getEntry(cleanPath.replace('\\', '/'))
                    ?: zip.entries().asSequence().firstOrNull { it.name.trimStart('/') == cleanPath }
                    ?: return@withContext false
                outFile.parentFile?.mkdirs()
                zip.getInputStream(entry).use { input ->
                    BufferedOutputStream(FileOutputStream(outFile)).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
