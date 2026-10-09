package com.nothing.files.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.zip.CRC32

data class ChecksumResult(
    val md5: String,
    val sha1: String,
    val sha256: String,
    val crc32: String,
    val fileSize: Long,
    val durationMs: Long
)

class ChecksumEngine {

    suspend fun calculateChecksums(
        file: File,
        onProgress: (bytesRead: Long, totalBytes: Long) -> Unit = { _, _ -> }
    ): ChecksumResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val md5Digest = MessageDigest.getInstance("MD5")
        val sha1Digest = MessageDigest.getInstance("SHA-1")
        val sha256Digest = MessageDigest.getInstance("SHA-256")
        val crc = CRC32()

        val totalSize = file.length()
        var totalRead = 0L
        val buffer = ByteArray(64 * 1024)

        FileInputStream(file).use { fis ->
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                md5Digest.update(buffer, 0, bytesRead)
                sha1Digest.update(buffer, 0, bytesRead)
                sha256Digest.update(buffer, 0, bytesRead)
                crc.update(buffer, 0, bytesRead)
                totalRead += bytesRead
                onProgress(totalRead, totalSize)
            }
        }

        val duration = System.currentTimeMillis() - startTime

        ChecksumResult(
            md5 = md5Digest.digest().joinToString("") { "%02x".format(it) },
            sha1 = sha1Digest.digest().joinToString("") { "%02x".format(it) },
            sha256 = sha256Digest.digest().joinToString("") { "%02x".format(it) },
            crc32 = "%08X".format(crc.value),
            fileSize = totalSize,
            durationMs = duration
        )
    }
}
