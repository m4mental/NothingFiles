package com.nothing.files.model

import androidx.compose.ui.graphics.Color
import com.nothing.files.ui.theme.NothingAccentBlue
import com.nothing.files.ui.theme.NothingAccentGreen
import com.nothing.files.ui.theme.NothingAccentOrange
import com.nothing.files.ui.theme.NothingAccentPurple
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingWhite
import java.util.Locale

enum class StorageCategoryType {
    IMAGES,
    VIDEOS,
    AUDIO,
    DOCUMENTS,
    APKS,
    ARCHIVES,
    DOWNLOADS,
    OTHER
}

data class StorageCategoryInfo(
    val type: StorageCategoryType,
    val title: String,
    val totalBytes: Long = 0L,
    val fileCount: Int = 0,
    val color: Color
) {
    val formattedSize: String
        get() {
            if (totalBytes <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(totalBytes.toDouble()) / Math.log10(1024.0)).toInt()
            val value = totalBytes / Math.pow(1024.0, digitGroups.toDouble())
            return String.format(Locale.US, "%.1f %s", value, units[digitGroups])
        }

    val dotMatrixCode: String
        get() = when (type) {
            StorageCategoryType.IMAGES -> "IMG"
            StorageCategoryType.VIDEOS -> "VID"
            StorageCategoryType.AUDIO -> "AUD"
            StorageCategoryType.DOCUMENTS -> "DOC"
            StorageCategoryType.APKS -> "APK"
            StorageCategoryType.ARCHIVES -> "ZIP"
            StorageCategoryType.DOWNLOADS -> "DL"
            StorageCategoryType.OTHER -> "OTH"
        }

    companion object {
        fun defaultCategories(): List<StorageCategoryInfo> = listOf(
            StorageCategoryInfo(StorageCategoryType.IMAGES, "Images", color = NothingRed),
            StorageCategoryInfo(StorageCategoryType.VIDEOS, "Videos", color = NothingAccentOrange),
            StorageCategoryInfo(StorageCategoryType.AUDIO, "Audio", color = NothingAccentGreen),
            StorageCategoryInfo(StorageCategoryType.DOCUMENTS, "Documents", color = NothingAccentBlue),
            StorageCategoryInfo(StorageCategoryType.APKS, "APKs", color = NothingAccentPurple),
            StorageCategoryInfo(StorageCategoryType.ARCHIVES, "Archives", color = NothingWhite),
            StorageCategoryInfo(StorageCategoryType.DOWNLOADS, "Downloads", color = NothingRed)
        )
    }
}
