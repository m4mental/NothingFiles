package com.nothing.files.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItemType {
    HOME,
    STORAGE_INTERNAL,
    STORAGE_ROOT,
    CATEGORY_AUDIO,
    CATEGORY_IMAGE,
    CATEGORY_VIDEO,
    CATEGORY_APK,
    CATEGORY_ARCHIVE,
    CATEGORY_DOCUMENT,
    CATEGORY_APP_MANAGER,
    ALL_FILES,
    RECENT_FILES,
    BOOKMARK
}

data class NavDrawerItem(
    val id: String,
    val type: NavItemType,
    val title: String,
    val subtitle: String? = null,
    val path: String? = null,
    val icon: ImageVector,
    val isPinned: Boolean = false,
    val count: Int = 0,
    val totalBytes: Long = 0L
)
