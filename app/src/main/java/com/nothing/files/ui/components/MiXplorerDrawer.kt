package com.nothing.files.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.model.NavItemType
import com.nothing.files.ui.theme.LedOff
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingTextTertiary
import com.nothing.files.ui.theme.NothingWhite

data class DrawerStorageInfo(
    val title: String,
    val path: String,
    val totalSize: String,
    val freeSize: String,
    val usedPercent: Int,
    val isRoot: Boolean = false
)

data class DrawerBookmark(
    val name: String,
    val path: String
)

@Composable
fun MiXplorerDrawer(
    storageInfo: DrawerStorageInfo,
    rootStorageInfo: DrawerStorageInfo?,
    bookmarks: List<DrawerBookmark>,
    onSelectNav: (NavItemType, String?) -> Unit,
    onOpenInNewTab: (NavItemType, String?) -> Unit = { type, path -> onSelectNav(type, path) },
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }
    var sortAscending by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(320.dp)
            .background(NothingBlack)
            .padding(vertical = 12.dp)
    ) {
        // Drawer Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSelectNav(NavItemType.HOME, null) }
            ) {
                Text(
                    text = "▼  BOOKMARKS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp,
                    color = NothingWhite
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { sortAscending = !sortAscending },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = if (!sortAscending) NothingRed else NothingTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("App Manager", color = NothingWhite, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showMoreMenu = false
                                onSelectNav(NavItemType.CATEGORY_APP_MANAGER, null)
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Recent Files", color = NothingWhite, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showMoreMenu = false
                                onSelectNav(NavItemType.RECENT_FILES, null)
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("All Files", color = NothingWhite, fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showMoreMenu = false
                                onSelectNav(NavItemType.ALL_FILES, null)
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = NothingBorder, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // 1. Bookmarks Section (Hearts)
            items(bookmarks) { b ->
                DrawerItemRow(
                    icon = Icons.Default.FavoriteBorder,
                    title = b.name,
                    subtitle = b.path,
                    badgeText = null,
                    onClick = { onSelectNav(NavItemType.BOOKMARK, b.path) },
                    onLongClick = { onOpenInNewTab(NavItemType.BOOKMARK, b.path) }
                )
            }

            if (bookmarks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = NothingBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // 2. Storage Section
            item {
                DrawerStorageRow(
                    icon = Icons.Default.PhoneAndroid,
                    title = storageInfo.title,
                    subtitle = "${storageInfo.totalSize}, ${storageInfo.freeSize} Free\n${storageInfo.path}",
                    usedPercent = storageInfo.usedPercent,
                    onClick = { onSelectNav(NavItemType.STORAGE_INTERNAL, storageInfo.path) },
                    onLongClick = { onOpenInNewTab(NavItemType.STORAGE_INTERNAL, storageInfo.path) }
                )
            }

            if (rootStorageInfo != null) {
                item {
                    DrawerStorageRow(
                        icon = Icons.Default.Tag,
                        title = rootStorageInfo.title,
                        subtitle = "${rootStorageInfo.totalSize}, ${rootStorageInfo.freeSize} Free",
                        usedPercent = rootStorageInfo.usedPercent,
                        onClick = { onSelectNav(NavItemType.STORAGE_ROOT, "/") },
                        onLongClick = { onOpenInNewTab(NavItemType.STORAGE_ROOT, "/") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = NothingBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // 3. Virtual Categories (Exact from MiXplorer Screenshot 2)
            item {
                DrawerItemRow(
                    icon = Icons.Default.MusicNote,
                    title = "Audio",
                    subtitle = "content://mix/audio",
                    badgeText = "AUD",
                    onClick = { onSelectNav(NavItemType.CATEGORY_AUDIO, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_AUDIO, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.Image,
                    title = "Image",
                    subtitle = "content://mix/image",
                    badgeText = "IMG",
                    onClick = { onSelectNav(NavItemType.CATEGORY_IMAGE, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_IMAGE, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.Movie,
                    title = "Video",
                    subtitle = "content://mix/video",
                    badgeText = "VID",
                    onClick = { onSelectNav(NavItemType.CATEGORY_VIDEO, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_VIDEO, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.Android,
                    title = "APK",
                    subtitle = "content://mix/apk",
                    badgeText = "APK",
                    onClick = { onSelectNav(NavItemType.CATEGORY_APK, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_APK, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.Archive,
                    title = "Archive",
                    subtitle = "content://mix/archive",
                    badgeText = "ARC",
                    onClick = { onSelectNav(NavItemType.CATEGORY_ARCHIVE, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_ARCHIVE, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.Description,
                    title = "Document",
                    subtitle = "content://mix/document",
                    badgeText = "DOC",
                    onClick = { onSelectNav(NavItemType.CATEGORY_DOCUMENT, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_DOCUMENT, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.Apps,
                    title = "App",
                    subtitle = "content://mix/app",
                    badgeText = "APPS",
                    onClick = { onSelectNav(NavItemType.CATEGORY_APP_MANAGER, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.CATEGORY_APP_MANAGER, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.FolderShared,
                    title = "All Files",
                    subtitle = "content://mix/all",
                    badgeText = "ALL",
                    onClick = { onSelectNav(NavItemType.ALL_FILES, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.ALL_FILES, null) }
                )
            }
            item {
                DrawerItemRow(
                    icon = Icons.Default.History,
                    title = "Recent files",
                    subtitle = "content://mix/recent",
                    badgeText = "REC",
                    onClick = { onSelectNav(NavItemType.RECENT_FILES, null) },
                    onLongClick = { onOpenInNewTab(NavItemType.RECENT_FILES, null) }
                )
            }
        }

        HorizontalDivider(color = NothingBorder, thickness = 1.dp)

        // Drawer Bottom Bar (Screenshot 2: Refresh, Addons/Tools, Layout/All)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSelectNav(NavItemType.HOME, null) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = NothingWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = { onSelectNav(NavItemType.CATEGORY_APP_MANAGER, null) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "App Manager",
                    tint = NothingWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = { onSelectNav(NavItemType.ALL_FILES, null) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderShared,
                    contentDescription = "All Files",
                    tint = NothingWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerStorageRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    usedPercent: Int,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    NothingCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = NothingSurface,
        borderColor = NothingBorder,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NothingWhite,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NothingWhite
                    )
                    Text(
                        text = subtitle,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NothingTextSecondary,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dot Matrix mini progress bar
            DotMatrixProgressBar(
                percentage = usedPercent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DrawerItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    badgeText: String?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NothingWhite,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = NothingWhite
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NothingTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (badgeText != null) {
            Text(
                text = badgeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = NothingRed
            )
        }
    }
}
