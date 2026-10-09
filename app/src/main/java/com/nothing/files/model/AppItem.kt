package com.nothing.files.model

import android.graphics.drawable.Drawable
import java.util.Locale

data class AppItem(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val sourceDir: String,
    val apkSize: Long,
    val isSystemApp: Boolean,
    val minSdk: Int,
    val targetSdk: Int,
    val uid: Int,
    val firstInstallTime: Long,
    val lastUpdateTime: Long
) {
    val formattedSize: String
        get() {
            if (apkSize <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB")
            val digitGroups = (Math.log10(apkSize.toDouble()) / Math.log10(1024.0)).toInt()
            val value = apkSize / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
        }
}
