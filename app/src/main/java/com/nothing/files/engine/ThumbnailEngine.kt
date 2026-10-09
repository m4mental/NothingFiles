package com.nothing.files.engine

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.util.Size
import com.nothing.files.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import android.graphics.pdf.PdfRenderer

object ThumbnailEngine {
    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = (maxMemoryKb / 8).coerceIn(16 * 1024, 64 * 1024)

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getFromCache(key: String): Bitmap? = synchronized(memoryCache) {
        memoryCache.get(key)
    }

    fun putInCache(key: String, bitmap: Bitmap) {
        synchronized(memoryCache) {
            memoryCache.put(key, bitmap)
        }
    }

    fun clearCache() {
        synchronized(memoryCache) {
            memoryCache.evictAll()
        }
    }

    suspend fun loadFileThumbnail(context: Context, file: FileItem, targetSize: Int = 256): Bitmap? = withContext(Dispatchers.IO) {
        when {
            file.isDirectory -> null
            file.isApk -> loadApkIcon(context, file.path, targetSize)
            file.isVideo -> loadVideoThumbnail(file.path, targetSize)
            file.extension == "pdf" -> loadPdfThumbnail(file.path, targetSize)
            file.isImage -> loadImageThumbnail(file.path, targetSize)
            else -> null
        }
    }

    suspend fun loadApkIcon(context: Context, path: String, targetSize: Int = 128): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null
        val cacheKey = "apk_${path}_${file.lastModified()}_$targetSize"
        getFromCache(cacheKey)?.let { return@withContext it }

        try {
            val pm = context.packageManager
            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(path, 0)
            }
            if (pi != null) {
                val appInfo = pi.applicationInfo ?: return@withContext null
                appInfo.sourceDir = path
                appInfo.publicSourceDir = path
                val iconDrawable = appInfo.loadIcon(pm) ?: return@withContext null
                val bitmap = drawableToBitmap(iconDrawable, targetSize, targetSize)
                putInCache(cacheKey, bitmap)
                return@withContext bitmap
            }
        } catch (_: Throwable) {}
        null
    }

    suspend fun loadInstalledAppIcon(context: Context, packageName: String, targetSize: Int = 128): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "app_$packageName"
        getFromCache(cacheKey)?.let { return@withContext it }

        try {
            val iconDrawable = context.packageManager.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(iconDrawable, targetSize, targetSize)
            putInCache(cacheKey, bitmap)
            return@withContext bitmap
        } catch (_: Throwable) {
            null
        }
    }

    private val appLabelCache = mutableMapOf<String, String>()

    fun getAppLabel(context: Context, packageName: String): String? {
        synchronized(appLabelCache) {
            if (appLabelCache.containsKey(packageName)) {
                val cached = appLabelCache[packageName]
                return if (cached.isNullOrEmpty()) null else cached
            }
        }
        return try {
            val pm = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            val label = pm.getApplicationLabel(appInfo).toString()
            synchronized(appLabelCache) { appLabelCache[packageName] = label }
            label
        } catch (_: Exception) {
            synchronized(appLabelCache) { appLabelCache[packageName] = "" }
            null
        }
    }

    suspend fun loadVideoThumbnail(path: String, targetSize: Int = 256): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null
        val cacheKey = "video_${path}_${file.lastModified()}_$targetSize"
        getFromCache(cacheKey)?.let { return@withContext it }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    val bmp = ThumbnailUtils.createVideoThumbnail(file, Size(targetSize, targetSize), null)
                    if (bmp != null) {
                        putInCache(cacheKey, bmp)
                        return@withContext bmp
                    }
                } catch (_: Throwable) {}
            }
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val bmp = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime
            retriever.release()
            if (bmp != null) {
                val scaled = if (bmp.width > targetSize || bmp.height > targetSize) {
                    val scale = targetSize.toFloat() / maxOf(bmp.width, bmp.height)
                    Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt().coerceAtLeast(1), (bmp.height * scale).toInt().coerceAtLeast(1), true)
                } else bmp
                putInCache(cacheKey, scaled)
                return@withContext scaled
            }
        } catch (_: Throwable) {}
        null
    }

    suspend fun loadPdfThumbnail(path: String, targetSize: Int = 280): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null
        val cacheKey = "pdf_${path}_${file.lastModified()}_$targetSize"
        getFromCache(cacheKey)?.let { return@withContext it }

        try {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (renderer.pageCount > 0) {
                        renderer.openPage(0).use { page ->
                            val targetW = targetSize
                            val targetH = ((targetW.toFloat() / page.width) * page.height).toInt().coerceIn(targetSize / 2, targetSize * 2)
                            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            canvas.drawColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            putInCache(cacheKey, bitmap)
                            return@withContext bitmap
                        }
                    }
                }
            }
        } catch (_: Throwable) {}
        null
    }

    suspend fun loadImageThumbnail(path: String, targetSize: Int = 256): Bitmap? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null
        val cacheKey = "img_${path}_${file.lastModified()}_$targetSize"
        getFromCache(cacheKey)?.let { return@withContext it }

        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)
            if (options.outWidth <= 0 || options.outHeight <= 0) return@withContext null

            var sampleSize = 1
            while (options.outWidth / (sampleSize * 2) >= targetSize && options.outHeight / (sampleSize * 2) >= targetSize) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val bmp = BitmapFactory.decodeFile(path, decodeOptions)
            if (bmp != null) {
                putInCache(cacheKey, bmp)
                return@withContext bmp
            }
        } catch (_: Throwable) {}
        null
    }

    fun drawableToBitmap(drawable: Drawable, width: Int = 128, height: Int = 128): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            return drawable.bitmap
        }
        val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else width
        val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else height
        val bitmap = Bitmap.createBitmap(w.coerceAtLeast(1), h.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
