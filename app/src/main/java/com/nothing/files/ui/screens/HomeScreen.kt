package com.nothing.files.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.CategoryScannerEngine
import com.nothing.files.engine.CategorySummary
import com.nothing.files.engine.StorageAnalyzerEngine
import com.nothing.files.engine.StorageSpaceAnalysis
import com.nothing.files.model.NavItemType
import com.nothing.files.ui.components.DotMatrixProgressBar
import com.nothing.files.ui.components.MiXplorerBottomBar
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenDrawer: () -> Unit,
    onNavigateToStorage: () -> Unit,
    onOpenCategory: (NavItemType, String) -> Unit,
    onOpenAppManager: () -> Unit,
    onOpenStorageAnalyzer: () -> Unit,
    onOpenWebShare: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scannerEngine = remember { CategoryScannerEngine(context) }
    val storageEngine = remember { StorageAnalyzerEngine() }

    var storageSummary by remember { mutableStateOf<StorageSpaceAnalysis?>(null) }
    var categorySummaries by remember { mutableStateOf<Map<NavItemType, CategorySummary>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var showTopMenu by remember { mutableStateOf(false) }

    fun loadData() {
        isLoading = true
        scope.launch {
            storageSummary = storageEngine.analyzeStorage()
            categorySummaries = scannerEngine.getCategorySummaries()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // 1. Top Bar (Matching Screenshot 3)
        NothingCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Drawer",
                                tint = NothingWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Home",
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = NothingWhite
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = NothingWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${categorySummaries.size} categories",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = NothingTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = { showTopMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = NothingWhite
                        )
                    }

                    DropdownMenu(
                        expanded = showTopMenu,
                        onDismissRequest = { showTopMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Storage Analyzer", fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showTopMenu = false
                                onOpenStorageAnalyzer()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Web Share (HTTP)", fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showTopMenu = false
                                onOpenWebShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("App Manager", fontFamily = FontFamily.Monospace) },
                            onClick = {
                                showTopMenu = false
                                onOpenAppManager()
                            }
                        )
                    }
                }

                // Tab Row (Home active tab with Nothing Red underline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = NothingTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(
                        modifier = Modifier
                            .clickable { }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Home",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(2.dp)
                                .background(NothingRed)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Internal storage",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = NothingTextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onNavigateToStorage() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Sub-tab Navigation Row: <  ⌂  ...
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Drawer",
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = NothingWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = { showTopMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 2. Body: Storage Section & Categories 2-Column Grid (Screenshot 3)
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp)
                ) {
                    // Section Banner 1: STORAGE
                    item {
                        SectionHeaderBanner(title = "STORAGE")
                    }

                    // Internal Storage Tile (Screenshot 3)
                    item {
                        val usedText = storageSummary?.formattedUsed ?: "63.1 GiB"
                        val totalText = storageSummary?.formattedTotal ?: "227 GiB"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToStorage() }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Storage",
                                tint = NothingWhite,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Internal storage",
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = NothingWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$usedText / $totalText",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = NothingTextSecondary
                                )
                            }
                        }
                    }

                    // Section Banner 2: CATEGORIES (Screenshot 3)
                    item {
                        SectionHeaderBanner(title = "CATEGORIES")
                    }

                    // 2-Column Category Grid Items
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Row 1: App & Audio
                            Row(modifier = Modifier.fillMaxWidth()) {
                                CategoryGridTile(
                                    icon = Icons.Default.Apps,
                                    title = "App",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_APP_MANAGER]?.displaySubtitle ?: "6.21 GiB | 354",
                                    onClick = onOpenAppManager,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                CategoryGridTile(
                                    icon = Icons.Default.MusicNote,
                                    title = "Audio",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_AUDIO]?.displaySubtitle ?: "0 B | 0",
                                    onClick = { onOpenCategory(NavItemType.CATEGORY_AUDIO, "Audio") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row 2: Document & Image
                            Row(modifier = Modifier.fillMaxWidth()) {
                                CategoryGridTile(
                                    icon = Icons.Default.Description,
                                    title = "Document",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_DOCUMENT]?.displaySubtitle ?: "0 B | 0",
                                    onClick = { onOpenCategory(NavItemType.CATEGORY_DOCUMENT, "Documents") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                CategoryGridTile(
                                    icon = Icons.Default.Image,
                                    title = "Image",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_IMAGE]?.displaySubtitle ?: "0 B | 0",
                                    onClick = { onOpenCategory(NavItemType.CATEGORY_IMAGE, "Images") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row 3: Video & Archive
                            Row(modifier = Modifier.fillMaxWidth()) {
                                CategoryGridTile(
                                    icon = Icons.Default.Movie,
                                    title = "Video",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_VIDEO]?.displaySubtitle ?: "0 B | 0",
                                    onClick = { onOpenCategory(NavItemType.CATEGORY_VIDEO, "Videos") },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                CategoryGridTile(
                                    icon = Icons.Default.Archive,
                                    title = "Archive",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_ARCHIVE]?.displaySubtitle ?: "ZIP / RAR / 7Z",
                                    onClick = { onOpenCategory(NavItemType.CATEGORY_ARCHIVE, "Archives") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row 4: APK
                            Row(modifier = Modifier.fillMaxWidth()) {
                                CategoryGridTile(
                                    icon = Icons.Default.Android,
                                    title = "APK",
                                    subtitle = categorySummaries[NavItemType.CATEGORY_APK]?.displaySubtitle ?: "Installers",
                                    onClick = { onOpenCategory(NavItemType.CATEGORY_APK, "APKs") },
                                    modifier = Modifier.weight(0.5f)
                                )
                                Spacer(modifier = Modifier.weight(0.5f))
                            }
                        }
                    }
                }
            }
        }

        // 3. Persistent Bottom Bar (Screenshot 1 & 3)
        MiXplorerBottomBar(
            onSearchClick = { onNavigateToStorage() },
            onSelectViewMode = { onNavigateToStorage() },
            onViewOptionsClick = { showTopMenu = true },
            onAddAction = { onNavigateToStorage() },
            onRefreshClick = { loadData() },
            onSelectSortOption = { onNavigateToStorage() },
            onSortOptionsClick = { onNavigateToStorage() },
            onSelectAllClick = { onNavigateToStorage() }
        )
    }
}

@Composable
private fun SectionHeaderBanner(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NothingSurfaceElevated)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = NothingTextSecondary
        )
    }
}

@Composable
private fun CategoryGridTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NothingWhite,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = NothingWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = NothingTextSecondary
            )
        }
    }
}
