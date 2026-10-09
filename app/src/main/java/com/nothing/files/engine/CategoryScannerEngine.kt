package com.nothing.files.engine

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.nothing.files.model.FileItem
import com.nothing.files.model.NavItemType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

data class CategorySummary(
    val type: NavItemType,
    val title: String,
    val count: Int,
    val totalBytes: Long
) {
    val formattedSize: String
        get() {
            if (totalBytes <= 0) return "0 B"
            val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
            val digitGroups = (Math.log10(totalBytes.toDouble()) / Math.log10(1024.0)).toInt()
            val value = totalBytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.2f %s", value, units[digitGroups])
        }

    val displaySubtitle: String
        get() = "$formattedSize | $count"
}

class CategoryScannerEngine(private val context: Context) {

    suspend fun getCategorySummaries(): Map<NavItemType, CategorySummary> = withContext(Dispatchers.IO) {
        val summaries = mutableMapOf<NavItemType, CategorySummary>()

        // 1. Audio
        val (audioCount, audioBytes) = queryMediaStore(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null
        )
        summaries[NavItemType.CATEGORY_AUDIO] = CategorySummary(
            NavItemType.CATEGORY_AUDIO,
            "Audio",
            audioCount,
            audioBytes
        )

        // 2. Images
        val (imgCount, imgBytes) = queryMediaStore(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null
        )
        summaries[NavItemType.CATEGORY_IMAGE] = CategorySummary(
            NavItemType.CATEGORY_IMAGE,
            "Image",
            imgCount,
            imgBytes
        )

        // 3. Videos
        val (vidCount, vidBytes) = queryMediaStore(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null
        )
        summaries[NavItemType.CATEGORY_VIDEO] = CategorySummary(
            NavItemType.CATEGORY_VIDEO,
            "Video",
            vidCount,
            vidBytes
        )

        // 4. Documents, APKs, Archives via fast file scanning in common locations
        val root = Environment.getExternalStorageDirectory()
        var docCount = 0
        var docBytes = 0L
        var apkCount = 0
        var apkBytes = 0L
        var arcCount = 0
        var arcBytes = 0L

        try {
            val commonDirs = listOf(
                File(root, "Download"),
                File(root, "Documents"),
                File(root, "PHONE_CONTROL")
            )
            for (dir in commonDirs) {
                if (!dir.exists()) continue
                dir.walkTopDown()
                    .maxDepth(4)
                    .filter { it.isFile && !it.name.startsWith(".") }
                    .forEach { f ->
                        val len = f.length()
                        val ext = f.extension.lowercase()
                        when {
                            ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub") -> {
                                docCount++
                                docBytes += len
                            }
                            ext == "apk" -> {
                                apkCount++
                                apkBytes += len
                            }
                            ext in listOf("zip", "rar", "7z", "tar", "gz") -> {
                                arcCount++
                                arcBytes += len
                            }
                        }
                    }
            }
        } catch (_: Exception) {}

        summaries[NavItemType.CATEGORY_DOCUMENT] = CategorySummary(NavItemType.CATEGORY_DOCUMENT, "Document", docCount, docBytes)
        summaries[NavItemType.CATEGORY_APK] = CategorySummary(NavItemType.CATEGORY_APK, "APK", apkCount, apkBytes)
        summaries[NavItemType.CATEGORY_ARCHIVE] = CategorySummary(NavItemType.CATEGORY_ARCHIVE, "Archive", arcCount, arcBytes)

        summaries
    }

    suspend fun getFilesForCategory(type: NavItemType): List<FileItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FileItem>()

        when (type) {
            NavItemType.CATEGORY_AUDIO -> {
                list.addAll(queryMediaFiles(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI))
            }
            NavItemType.CATEGORY_IMAGE -> {
                list.addAll(queryMediaFiles(MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
            }
            NavItemType.CATEGORY_VIDEO -> {
                list.addAll(queryMediaFiles(MediaStore.Video.Media.EXTERNAL_CONTENT_URI))
            }
            NavItemType.RECENT_FILES -> {
                list.addAll(queryRecentFiles())
            }
            NavItemType.CATEGORY_DOCUMENT, NavItemType.CATEGORY_APK, NavItemType.CATEGORY_ARCHIVE -> {
                val root = Environment.getExternalStorageDirectory()
                val targetExtensions = when (type) {
                    NavItemType.CATEGORY_DOCUMENT -> listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "epub")
                    NavItemType.CATEGORY_APK -> listOf("apk")
                    NavItemType.CATEGORY_ARCHIVE -> listOf("zip", "rar", "7z", "tar", "gz", "bz2")
                    else -> emptyList()
                }

                root.walkTopDown()
                    .onEnter { !it.name.startsWith(".") && it.name != "Android" }
                    .filter { it.isFile && it.extension.lowercase() in targetExtensions }
                    .take(300)
                    .forEach { list.add(FileItem.fromFile(it)) }
            }
            else -> {}
        }

        list.sortedByDescending { it.lastModified }
    }

    private fun queryMediaStore(uri: Uri, selection: String?, selectionArgs: Array<String>?): Pair<Int, Long> {
        var count = 0
        var bytes = 0L
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                while (cursor.moveToNext()) {
                    count++
                    if (sizeCol != -1) {
                        bytes += cursor.getLong(sizeCol)
                    }
                }
            }
        } catch (_: Exception) {}
        return Pair(count, bytes)
    }

    private fun queryMediaFiles(uri: Uri): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        try {
            val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT 400"
            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                while (cursor.moveToNext()) {
                    if (dataCol != -1) {
                        val path = cursor.getString(dataCol)
                        if (path != null) {
                            val f = File(path)
                            if (f.exists()) {
                                list.add(FileItem.fromFile(f))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }

    private fun queryRecentFiles(): List<FileItem> {
        val list = mutableListOf<FileItem>()
        val uri = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT 100"
        try {
            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                while (cursor.moveToNext()) {
                    if (dataCol != -1) {
                        val path = cursor.getString(dataCol)
                        if (path != null) {
                            val f = File(path)
                            if (f.exists() && f.isFile) {
                                list.add(FileItem.fromFile(f))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }
}
