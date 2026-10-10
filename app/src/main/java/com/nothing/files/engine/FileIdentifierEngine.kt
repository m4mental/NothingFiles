package com.nothing.files.engine

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.nothing.files.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class FileCategory(val label: String, val dotTag: String) {
    APK("Android App", "APK"),
    AUDIO("Audio", "AUD"),
    VIDEO("Video", "VID"),
    IMAGE("Image", "IMG"),
    DOCUMENT("Document", "DOC"),
    CODE("Source Code", "CODE"),
    ARCHIVE("Archive", "ARC"),
    DATABASE("Database", "DB"),
    FONT("Font", "FONT"),
    CERTIFICATE("Security / Key", "KEY"),
    EXECUTABLE("Binary / Executable", "BIN"),
    CONFIG("Config File", "CONF"),
    GENERIC("File", "FILE")
}

data class AppHandlerInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?
)

data class ApkPackageDetails(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val iconBitmap: Bitmap?
)

object FileIdentifierEngine {

    private val APK_EXTENSIONS = setOf("apk", "xapk", "apks", "aab")
    private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "wma", "mid", "midi", "amr")
    private val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "avi", "mov", "flv", "3gp", "ts", "wmv", "m4v")
    private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif", "bmp", "svg", "heic", "heif", "ico", "tiff", "tif")
    private val DOCUMENT_EXTENSIONS = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "epub", "rtf", "odt", "ods", "odp", "pages", "numbers", "key")
    private val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz", "tbz2", "txz", "jar", "iso", "cab", "lzma")
    private val DATABASE_EXTENSIONS = setOf("db", "sqlite", "sqlite3", "sql", "realm")
    private val FONT_EXTENSIONS = setOf("ttf", "otf", "woff", "woff2")
    private val CERTIFICATE_EXTENSIONS = setOf("pem", "crt", "cer", "der", "p12", "pfx", "key", "keystore", "jks")
    private val EXECUTABLE_EXTENSIONS = setOf("dex", "so", "bin", "elf", "img", "rom")
    private val CONFIG_EXTENSIONS = setOf("yaml", "yml", "ini", "conf", "config", "properties", "gradle", "toml", "env")
    private val CODE_EXTENSIONS = setOf(
        "txt", "log", "json", "xml", "html", "htm", "css", "scss", "sass", "js", "ts", "jsx", "tsx",
        "kt", "kts", "java", "py", "pyw", "sh", "bash", "zsh", "c", "h", "cpp", "hpp", "cc", "cxx",
        "cs", "go", "rs", "swift", "dart", "php", "rb", "lua", "r", "m", "mm", "asm", "s", "pl",
        "csv", "tsv", "md", "markdown"
    )

    fun getCategory(ext: String): FileCategory {
        val cleanExt = ext.removePrefix(".").lowercase(Locale.ROOT)
        return when {
            cleanExt in APK_EXTENSIONS -> FileCategory.APK
            cleanExt in AUDIO_EXTENSIONS -> FileCategory.AUDIO
            cleanExt in VIDEO_EXTENSIONS -> FileCategory.VIDEO
            cleanExt in IMAGE_EXTENSIONS -> FileCategory.IMAGE
            cleanExt in DOCUMENT_EXTENSIONS -> FileCategory.DOCUMENT
            cleanExt in ARCHIVE_EXTENSIONS -> FileCategory.ARCHIVE
            cleanExt in DATABASE_EXTENSIONS -> FileCategory.DATABASE
            cleanExt in FONT_EXTENSIONS -> FileCategory.FONT
            cleanExt in CERTIFICATE_EXTENSIONS -> FileCategory.CERTIFICATE
            cleanExt in EXECUTABLE_EXTENSIONS -> FileCategory.EXECUTABLE
            cleanExt in CONFIG_EXTENSIONS -> FileCategory.CONFIG
            cleanExt in CODE_EXTENSIONS -> FileCategory.CODE
            else -> FileCategory.GENERIC
        }
    }

    fun getDotMatrixTag(ext: String, isDirectory: Boolean): String {
        if (isDirectory) return "DIR"
        val cleanExt = ext.removePrefix(".").lowercase(Locale.ROOT)
        return when (cleanExt) {
            "apk" -> "APK"
            "pdf" -> "PDF"
            "zip" -> "ZIP"
            "7z" -> "7Z"
            "rar" -> "RAR"
            "tar", "gz" -> "TAR"
            "mp4", "mkv", "webm", "avi" -> "VID"
            "mp3", "flac", "wav", "m4a", "opus" -> "AUD"
            "jpg", "jpeg", "png", "webp", "gif" -> "IMG"
            "db", "sqlite", "sqlite3" -> "SQL"
            "kt", "kts" -> "KT"
            "java" -> "JAVA"
            "py" -> "PY"
            "js", "ts" -> "JS"
            "json" -> "JSON"
            "xml" -> "XML"
            "html", "htm" -> "HTML"
            "css" -> "CSS"
            "sh", "bash" -> "SH"
            "md" -> "MD"
            "doc", "docx" -> "DOC"
            "xls", "xlsx" -> "XLS"
            "ppt", "pptx" -> "PPT"
            "txt", "log" -> "TXT"
            else -> getCategory(cleanExt).dotTag
        }
    }

    fun getTypeDescription(ext: String): String {
        val cleanExt = ext.removePrefix(".").lowercase(Locale.ROOT)
        return when (cleanExt) {
            "apk" -> "Android Package (APK)"
            "xapk", "apks" -> "Android App Bundle"
            "aab" -> "Android App Bundle"
            "pdf" -> "PDF Document"
            "mp4" -> "MPEG-4 Video"
            "mkv" -> "Matroska Video"
            "webm" -> "WebM Video"
            "avi" -> "Audio Video Interleave"
            "mov" -> "QuickTime Video"
            "flv" -> "Flash Video"
            "3gp" -> "3GPP Video"
            "mp3" -> "MP3 Audio"
            "flac" -> "FLAC Lossless Audio"
            "wav" -> "Waveform Audio"
            "m4a" -> "MPEG-4 Audio"
            "aac" -> "AAC Audio"
            "ogg" -> "Ogg Vorbis Audio"
            "opus" -> "Opus Audio"
            "jpg", "jpeg" -> "JPEG Image"
            "png" -> "PNG Image"
            "webp" -> "WebP Image"
            "gif" -> "Animated GIF Image"
            "svg" -> "SVG Vector Graphic"
            "heic", "heif" -> "High Efficiency Image"
            "ico" -> "Icon Image"
            "bmp" -> "Bitmap Image"
            "doc", "docx" -> "Microsoft Word Document"
            "xls", "xlsx" -> "Excel Spreadsheet"
            "ppt", "pptx" -> "PowerPoint Presentation"
            "epub" -> "EPUB Electronic Book"
            "txt" -> "Plain Text Document"
            "log" -> "System / Application Log"
            "md", "markdown" -> "Markdown Document"
            "json" -> "JSON Document"
            "xml" -> "XML Document"
            "html", "htm" -> "HTML Webpage"
            "css" -> "CSS Stylesheet"
            "js" -> "JavaScript File"
            "ts" -> "TypeScript File"
            "kt", "kts" -> "Kotlin Source Code"
            "java" -> "Java Source Code"
            "py", "pyw" -> "Python Script"
            "sh", "bash" -> "Shell Script"
            "sql" -> "SQL Database Script"
            "c", "h" -> "C Source Code"
            "cpp", "hpp", "cc" -> "C++ Source Code"
            "cs" -> "C# Source Code"
            "go" -> "Go Source Code"
            "rs" -> "Rust Source Code"
            "dart" -> "Dart Source Code"
            "yaml", "yml" -> "YAML Configuration"
            "properties", "ini", "conf" -> "Configuration File"
            "gradle" -> "Gradle Build Script"
            "zip" -> "ZIP Compressed Archive"
            "rar" -> "RAR Compressed Archive"
            "7z" -> "7-Zip Compressed Archive"
            "tar" -> "Tape Archive (TAR)"
            "gz", "tgz" -> "GZIP Compressed Archive"
            "bz2" -> "BZIP2 Compressed Archive"
            "xz" -> "XZ Compressed Archive"
            "db", "sqlite", "sqlite3" -> "SQLite Database"
            "ttf", "otf" -> "Font File"
            "so" -> "ELF Shared Object Library"
            "dex" -> "Dalvik Executable"
            "cer", "crt", "pem" -> "Security Certificate"
            "key", "keystore", "jks" -> "Cryptographic Key / Store"
            "iso", "img" -> "Disk Image"
            else -> if (cleanExt.isNotEmpty()) "${cleanExt.uppercase(Locale.ROOT)} File" else "Binary File"
        }
    }

    fun getMimeType(ext: String): String {
        val cleanExt = ext.removePrefix(".").lowercase(Locale.ROOT)
        return when (cleanExt) {
            "apk", "xapk", "apks" -> "application/vnd.android.package-archive"
            "pdf" -> "application/pdf"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "flv" -> "video/x-flv"
            "3gp" -> "video/3gpp"
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "opus" -> "audio/opus"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "bmp" -> "image/bmp"
            "heic" -> "image/heic"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "epub" -> "application/epub+zip"
            "txt", "log", "conf", "ini", "properties" -> "text/plain"
            "md", "markdown" -> "text/markdown"
            "json" -> "application/json"
            "xml" -> "text/xml"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "ts" -> "application/typescript"
            "kt", "kts" -> "text/x-kotlin"
            "java" -> "text/x-java-source"
            "py" -> "text/x-python"
            "sh", "bash" -> "application/x-sh"
            "sql" -> "application/sql"
            "c", "h" -> "text/x-c"
            "cpp", "hpp" -> "text/x-c++"
            "yaml", "yml" -> "text/yaml"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz", "tgz" -> "application/gzip"
            "bz2" -> "application/x-bzip2"
            "xz" -> "application/x-xz"
            "db", "sqlite", "sqlite3" -> "application/x-sqlite3"
            "ttf" -> "font/ttf"
            "otf" -> "font/otf"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(cleanExt) ?: "*/*"
        }
    }

    suspend fun getApkDetails(context: Context, path: String): ApkPackageDetails? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null
            val pm = context.packageManager
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(path, 0)
            } ?: return@withContext null

            val appInfo = pi.applicationInfo ?: return@withContext null
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path

            val label = try {
                appInfo.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
            } catch (_: Exception) {
                file.nameWithoutExtension
            }

            val iconDrawable = try {
                appInfo.loadIcon(pm)
            } catch (_: Exception) {
                null
            }

            val iconBitmap = iconDrawable?.let { ThumbnailEngine.drawableToBitmap(it, 160, 160) }
            val versionName = pi.versionName ?: "1.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pi.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pi.versionCode.toLong()
            }
            val targetSdk = appInfo.targetSdkVersion
            val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) appInfo.minSdkVersion else 21

            ApkPackageDetails(
                appName = label,
                packageName = pi.packageName ?: "",
                versionName = versionName,
                versionCode = versionCode,
                targetSdkVersion = targetSdk,
                minSdkVersion = minSdk,
                iconBitmap = iconBitmap
            )
        } catch (_: Exception) {
            null
        }
    }

    fun resolveFileUri(context: Context, fileItem: FileItem): Uri {
        val file = File(fileItem.path)
        // 1. Direct FileProvider
        try {
            return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (_: Exception) {
            // 2. Cache copy for root-only / private paths
            return getCacheProviderUri(context, file)
        }
    }

    private fun getCacheProviderUri(context: Context, file: File): Uri {
        val cacheDir = File(context.cacheDir, "opened_files").apply { mkdirs() }
        val cacheTarget = File(cacheDir, file.name)
        try {
            if (file.canRead()) {
                file.copyTo(cacheTarget, overwrite = true)
            } else {
                // Root copy if necessary
                val cmd = "cp '${file.absolutePath}' '${cacheTarget.absolutePath}' && chmod 666 '${cacheTarget.absolutePath}'"
                kotlinx.coroutines.runBlocking {
                    RootShellEngine.executeCommand(cmd, runAsRoot = true)
                }
            }
            cacheTarget.setReadable(true, false)
            return FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheTarget)
        } catch (_: Exception) {
            return Uri.fromFile(file)
        }
    }

    fun getRelevantApps(context: Context, uri: Uri, mimeType: String): List<AppHandlerInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            PackageManager.MATCH_DEFAULT_ONLY
        }

        var list = try {
            pm.queryIntentActivities(intent, flags)
        } catch (_: Exception) {
            emptyList()
        }

        // If no direct matches and MIME type is specific (like video/mp4), try wildcard (video/*)
        if (list.isEmpty() && mimeType.contains('/')) {
            val wildcardMime = mimeType.substringBefore('/') + "/*"
            val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, wildcardMime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            list = try {
                pm.queryIntentActivities(fallbackIntent, flags)
            } catch (_: Exception) {
                emptyList()
            }
        }

        // Exclude our own app so we don't show recursive Nothing Files in the external apps list
        return list
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                AppHandlerInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    activityName = resolveInfo.activityInfo.name,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = resolveInfo.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    fun openWithApp(
        context: Context,
        uri: Uri,
        mimeType: String,
        packageName: String?,
        activityName: String?
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!packageName.isNullOrBlank()) {
                    if (!activityName.isNullOrBlank()) {
                        setClassName(packageName, activityName)
                    } else {
                        setPackage(packageName)
                    }
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot launch app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openSystemChooser(
        context: Context,
        uri: Uri,
        mimeType: String,
        title: String = "Open with"
    ) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    fun installApk(context: Context, fileItem: FileItem): Boolean {
        return try {
            val uri = resolveFileUri(context, fileItem)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to launch installer: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
