package com.nothing.files.engine

import android.os.Environment
import android.os.StatFs
import com.nothing.files.model.FileItem
import com.nothing.files.model.StorageCategoryInfo
import com.nothing.files.model.StorageCategoryType
import com.nothing.files.ui.theme.NothingAccentBlue
import com.nothing.files.ui.theme.NothingAccentGreen
import com.nothing.files.ui.theme.NothingAccentOrange
import com.nothing.files.ui.theme.NothingAccentPurple
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class StorageSpaceAnalysis(
    val totalBytes: Long,
    val usedBytes: Long,
    val freeBytes: Long,
    val usedPercent: Int,
    val categories: List<StorageCategoryInfo>,
    val largestFiles: List<FileItem>
) {
    val formattedTotal: String get() = formatSize(totalBytes)
    val formattedUsed: String get() = formatSize(usedBytes)
    val formattedFree: String get() = formatSize(freeBytes)

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
    }
}

class StorageAnalyzerEngine {

    suspend fun analyzeStorage(
        rootPath: String = Environment.getExternalStorageDirectory().absolutePath
    ): StorageSpaceAnalysis = withContext(Dispatchers.IO) {
        val stat = StatFs(rootPath)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0)
        val usedPercent = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes) * 100).toInt() else 0

        var imageBytes = 0L
        var imageCount = 0
        var videoBytes = 0L
        var videoCount = 0
        var audioBytes = 0L
        var audioCount = 0
        var docBytes = 0L
        var docCount = 0
        var apkBytes = 0L
        var apkCount = 0
        var archiveBytes = 0L
        var archiveCount = 0
        var otherBytes = 0L
        var otherCount = 0

        val allFiles = mutableListOf<File>()

        try {
            val rootDir = File(rootPath)
            rootDir.walkTopDown()
                .onEnter { !it.name.startsWith(".") && it.name != "Android" } // Skip Android/data due to SAF restrictions on newer OS
                .filter { it.isFile }
                .forEach { file ->
                    val len = file.length()
                    allFiles.add(file)
                    val ext = file.extension.lowercase()

                    when {
                        ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic") -> {
                            imageBytes += len
                            imageCount++
                        }
                        ext in listOf("mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "ts") -> {
                            videoBytes += len
                            videoCount++
                        }
                        ext in listOf("mp3", "flac", "wav", "m4a", "aac", "ogg", "opus") -> {
                            audioBytes += len
                            audioCount++
                        }
                        ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub") -> {
                            docBytes += len
                            docCount++
                        }
                        ext == "apk" -> {
                            apkBytes += len
                            apkCount++
                        }
                        ext in listOf("zip", "rar", "7z", "tar", "gz", "bz2") -> {
                            archiveBytes += len
                            archiveCount++
                        }
                        else -> {
                            otherBytes += len
                            otherCount++
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val categories = listOf(
            StorageCategoryInfo(StorageCategoryType.IMAGES, "Images", imageBytes, imageCount, NothingRed),
            StorageCategoryInfo(StorageCategoryType.VIDEOS, "Videos", videoBytes, videoCount, NothingAccentOrange),
            StorageCategoryInfo(StorageCategoryType.AUDIO, "Audio", audioBytes, audioCount, NothingAccentGreen),
            StorageCategoryInfo(StorageCategoryType.DOCUMENTS, "Docs", docBytes, docCount, NothingAccentBlue),
            StorageCategoryInfo(StorageCategoryType.APKS, "APKs", apkBytes, apkCount, NothingAccentPurple),
            StorageCategoryInfo(StorageCategoryType.ARCHIVES, "Archives", archiveBytes, archiveCount, NothingWhite)
        )

        val largestFiles = allFiles
            .sortedByDescending { it.length() }
            .take(50)
            .map { FileItem.fromFile(it) }

        StorageSpaceAnalysis(
            totalBytes = totalBytes,
            usedBytes = usedBytes,
            freeBytes = freeBytes,
            usedPercent = usedPercent,
            categories = categories,
            largestFiles = largestFiles
        )
    }
}
