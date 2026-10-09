package com.nothing.files.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ViewMode(val label: String) {
    COMPACT("Compact"),
    COLUMNED("Columned"),
    DETAILED("Detailed"),
    WRAPPED("Wrapped"),
    MINIMAL("Minimal"),
    GRID("Grid"),
    GALLERY("Gallery")
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val isHidden: Boolean = name.startsWith("."),
    val isSymlink: Boolean = false,
    val permissions: String = "rw-r--r--",
    val childCount: Int = 0
) {
    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "").lowercase()

    val isArchive: Boolean
        get() = extension in listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "apk", "jar")

    val isApk: Boolean
        get() = extension == "apk"

    val isImage: Boolean
        get() = extension in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic")

    val isVideo: Boolean
        get() = extension in listOf("mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "ts")

    val isAudio: Boolean
        get() = extension in listOf("mp3", "flac", "wav", "m4a", "aac", "ogg", "opus")

    val isTextOrCode: Boolean
        get() = extension in listOf(
            "txt", "log", "json", "xml", "html", "css", "js", "ts", "kt", "java", "py", "sh",
            "md", "c", "cpp", "h", "gradle", "properties", "yaml", "yml", "ini", "conf", "sql"
        )

    val isPdfOrDoc: Boolean
        get() = extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "epub")

    val formattedSize: String
        get() {
            if (isDirectory) {
                return if (childCount > 0) "$childCount items" else "Empty"
            }
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
            val value = size / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
            return sdf.format(Date(lastModified))
        }

    val dotMatrixTypeTag: String
        get() {
            return when {
                isDirectory -> "DIR"
                isApk -> "APK"
                isArchive -> "ARC"
                isImage -> "IMG"
                isVideo -> "VID"
                isAudio -> "AUD"
                isTextOrCode -> "CODE"
                isPdfOrDoc -> "DOC"
                else -> extension.take(4).uppercase().ifEmpty { "BIN" }
            }
        }

    companion object {
        fun fromFile(file: File): FileItem {
            val isDir = file.isDirectory
            val children = if (isDir) {
                try { file.list()?.size ?: 0 } catch (_: Exception) { 0 }
            } else 0

            var perms = ""
            perms += if (file.canRead()) "r" else "-"
            perms += if (file.canWrite()) "w" else "-"
            perms += if (file.canExecute()) "x" else "-"

            return FileItem(
                name = file.name.ifEmpty { file.path },
                path = file.absolutePath,
                isDirectory = isDir,
                size = if (isDir) 0L else file.length(),
                lastModified = file.lastModified(),
                isHidden = file.isHidden || file.name.startsWith("."),
                permissions = perms,
                childCount = children
            )
        }
    }
}
