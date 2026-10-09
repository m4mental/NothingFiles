package com.nothing.files

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.StorageAnalyzerEngine
import com.nothing.files.model.FileItem
import com.nothing.files.model.NavItemType
import com.nothing.files.model.TabInfo
import com.nothing.files.ui.components.DrawerBookmark
import com.nothing.files.ui.components.DrawerStorageInfo
import com.nothing.files.ui.components.MiXplorerDrawer
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.screens.AppManagerScreen
import com.nothing.files.ui.screens.CategoryFilesScreen
import com.nothing.files.ui.screens.FileExplorerScreen
import com.nothing.files.ui.screens.HexViewerScreen
import com.nothing.files.ui.screens.HomeScreen
import com.nothing.files.ui.screens.SettingsScreen
import com.nothing.files.ui.screens.StorageAnalyzerScreen
import com.nothing.files.ui.screens.TextEditorScreen
import com.nothing.files.ui.screens.WebShareScreen
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingFilesTheme
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.launch
import java.io.File

sealed class CurrentScreen {
    object Home : CurrentScreen()
    data class Explorer(val targetPath: String? = null) : CurrentScreen()
    data class CategoryFiles(val type: NavItemType, val title: String) : CurrentScreen()
    object AppManager : CurrentScreen()
    object StorageAnalyzer : CurrentScreen()
    object WebShare : CurrentScreen()
    data class TextEditor(val file: FileItem) : CurrentScreen()
    data class HexViewer(val file: FileItem) : CurrentScreen()
    object Settings : CurrentScreen()
}

class MainActivity : ComponentActivity() {

    private var hasStoragePermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        checkStoragePermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkStoragePermission()

        setContent {
            NothingFilesTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NothingBlack),
                    containerColor = NothingBlack
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        if (!hasStoragePermission) {
                            PermissionRequiredScreen(
                                onRequestPermission = { requestStoragePermission() }
                            )
                        } else {
                            MainDrawerAndContentRouter()
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            permissionLauncher.launch(permissions)
        }
    }
}

@Composable
fun MainDrawerAndContentRouter() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<CurrentScreen>(CurrentScreen.Home) }

    val internalPath = Environment.getExternalStorageDirectory().absolutePath

    // Shared multi-tab state for FileExplorerScreen across drawer and screen navigation
    val explorerTabs = remember {
        mutableStateListOf(TabInfo(title = "Internal storage", path = internalPath))
    }
    var activeTabIndex by remember { mutableStateOf(0) }

    fun formatStorageBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return String.format(java.util.Locale.US, "%.2f %s", value, units[digitGroups])
    }

    var internalStorageInfo by remember {
        mutableStateOf(run {
            val stat = android.os.StatFs(internalPath)
            val total = stat.totalBytes
            val free = stat.availableBytes
            val used = (total - free).coerceAtLeast(0)
            val percent = if (total > 0) ((used.toDouble() / total) * 100).toInt() else 0
            DrawerStorageInfo(
                title = "Internal storage",
                path = internalPath,
                totalSize = formatStorageBytes(total),
                freeSize = "${formatStorageBytes(free)} Free",
                usedPercent = percent
            )
        })
    }

    var rootStorageInfo by remember {
        mutableStateOf<DrawerStorageInfo?>(run {
            try {
                val stat = android.os.StatFs("/")
                val total = stat.totalBytes
                val free = stat.availableBytes
                val used = (total - free).coerceAtLeast(0)
                val percent = if (total > 0) ((used.toDouble() / total) * 100).toInt() else 100
                DrawerStorageInfo(
                    title = "Root",
                    path = "/",
                    totalSize = formatStorageBytes(total),
                    freeSize = "${formatStorageBytes(free)} Free",
                    usedPercent = percent,
                    isRoot = true
                )
            } catch (_: Exception) {
                DrawerStorageInfo(
                    title = "Root",
                    path = "/",
                    totalSize = "System Partition",
                    freeSize = "0 B Free",
                    usedPercent = 100,
                    isRoot = true
                )
            }
        })
    }

    val context = LocalContext.current
    var bookmarks by remember { mutableStateOf(com.nothing.files.engine.BookmarkManager.getBookmarks(context)) }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            bookmarks = com.nothing.files.engine.BookmarkManager.getBookmarks(context)
            try {
                val stat = android.os.StatFs(internalPath)
                val total = stat.totalBytes
                val free = stat.availableBytes
                val used = (total - free).coerceAtLeast(0)
                val percent = if (total > 0) ((used.toDouble() / total) * 100).toInt() else 0
                internalStorageInfo = DrawerStorageInfo(
                    title = "Internal storage",
                    path = internalPath,
                    totalSize = formatStorageBytes(total),
                    freeSize = "${formatStorageBytes(free)} Free",
                    usedPercent = percent
                )

                val rootStat = android.os.StatFs("/")
                val rTotal = rootStat.totalBytes
                val rFree = rootStat.availableBytes
                val rUsed = (rTotal - rFree).coerceAtLeast(0)
                val rPercent = if (rTotal > 0) ((rUsed.toDouble() / rTotal) * 100).toInt() else 100
                rootStorageInfo = DrawerStorageInfo(
                    title = "Root",
                    path = "/",
                    totalSize = formatStorageBytes(rTotal),
                    freeSize = "${formatStorageBytes(rFree)} Free",
                    usedPercent = rPercent,
                    isRoot = true
                )
            } catch (_: Exception) {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NothingBlack,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                MiXplorerDrawer(
                    storageInfo = internalStorageInfo,
                    rootStorageInfo = rootStorageInfo,
                    bookmarks = bookmarks,
                    onSelectNav = { type, path ->
                        scope.launch { drawerState.close() }
                        when (type) {
                            NavItemType.HOME -> currentScreen = CurrentScreen.Home
                            NavItemType.STORAGE_INTERNAL -> {
                                if (explorerTabs.isNotEmpty()) {
                                    explorerTabs[activeTabIndex] = explorerTabs[activeTabIndex].navigateTo(internalPath)
                                }
                                currentScreen = CurrentScreen.Explorer(internalPath)
                            }
                            NavItemType.STORAGE_ROOT -> {
                                if (explorerTabs.isNotEmpty()) {
                                    explorerTabs[activeTabIndex] = explorerTabs[activeTabIndex].navigateTo("/")
                                }
                                currentScreen = CurrentScreen.Explorer("/")
                            }
                            NavItemType.BOOKMARK -> if (path != null) {
                                if (explorerTabs.isNotEmpty()) {
                                    explorerTabs[activeTabIndex] = explorerTabs[activeTabIndex].navigateTo(path)
                                }
                                currentScreen = CurrentScreen.Explorer(path)
                            }
                            NavItemType.CATEGORY_APP_MANAGER -> currentScreen = CurrentScreen.AppManager
                            NavItemType.ALL_FILES -> {
                                if (explorerTabs.isNotEmpty()) {
                                    explorerTabs[activeTabIndex] = explorerTabs[activeTabIndex].navigateTo(internalPath)
                                }
                                currentScreen = CurrentScreen.Explorer(internalPath)
                            }
                            NavItemType.CATEGORY_AUDIO -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.CATEGORY_AUDIO, "Audio")
                            NavItemType.CATEGORY_IMAGE -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.CATEGORY_IMAGE, "Image")
                            NavItemType.CATEGORY_VIDEO -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.CATEGORY_VIDEO, "Video")
                            NavItemType.CATEGORY_APK -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.CATEGORY_APK, "APK")
                            NavItemType.CATEGORY_ARCHIVE -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.CATEGORY_ARCHIVE, "Archive")
                            NavItemType.CATEGORY_DOCUMENT -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.CATEGORY_DOCUMENT, "Document")
                            NavItemType.RECENT_FILES -> currentScreen = CurrentScreen.CategoryFiles(NavItemType.RECENT_FILES, "Recent files")
                        }
                    },
                    onOpenInNewTab = { type, path ->
                        scope.launch { drawerState.close() }
                        val (targetTitle, targetPath) = when (type) {
                            NavItemType.HOME -> Pair("Internal storage", internalPath)
                            NavItemType.STORAGE_INTERNAL -> Pair("Internal storage", internalPath)
                            NavItemType.STORAGE_ROOT -> Pair("Root", "/")
                            NavItemType.BOOKMARK -> Pair(path?.substringAfterLast('/')?.ifEmpty { "Bookmark" } ?: "Bookmark", path ?: internalPath)
                            NavItemType.CATEGORY_AUDIO -> Pair("Music", File(internalPath, "Music").let { if (it.exists()) it.absolutePath else internalPath })
                            NavItemType.CATEGORY_IMAGE -> Pair("DCIM", File(internalPath, "DCIM").let { if (it.exists()) it.absolutePath else internalPath })
                            NavItemType.CATEGORY_VIDEO -> Pair("Movies", File(internalPath, "Movies").let { if (it.exists()) it.absolutePath else internalPath })
                            NavItemType.CATEGORY_DOCUMENT -> Pair("Documents", File(internalPath, "Documents").let { if (it.exists()) it.absolutePath else internalPath })
                            NavItemType.CATEGORY_ARCHIVE -> Pair("Download", File(internalPath, "Download").let { if (it.exists()) it.absolutePath else internalPath })
                            NavItemType.CATEGORY_APK -> Pair("Download", File(internalPath, "Download").let { if (it.exists()) it.absolutePath else internalPath })
                            NavItemType.ALL_FILES -> Pair("All Files", internalPath)
                            NavItemType.RECENT_FILES -> Pair("Recent", internalPath)
                            NavItemType.CATEGORY_APP_MANAGER -> Pair("App Manager", internalPath)
                        }

                        val existingIndex = explorerTabs.indexOfFirst { it.path == targetPath }
                        if (existingIndex >= 0) {
                            activeTabIndex = existingIndex
                        } else {
                            explorerTabs.add(TabInfo(title = targetTitle, path = targetPath))
                            activeTabIndex = explorerTabs.size - 1
                        }
                        currentScreen = CurrentScreen.Explorer(targetPath)
                        android.widget.Toast.makeText(context, "Opened '$targetTitle' in new tab", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is CurrentScreen.Home -> {
                    HomeScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToStorage = { currentScreen = CurrentScreen.Explorer(internalPath) },
                        onNavigateToPath = { path ->
                            if (explorerTabs.isNotEmpty()) {
                                explorerTabs[activeTabIndex] = explorerTabs[activeTabIndex].navigateTo(path)
                            }
                            currentScreen = CurrentScreen.Explorer(path)
                        },
                        onOpenCategory = { type, title ->
                            currentScreen = CurrentScreen.CategoryFiles(type, title)
                        },
                        onOpenAppManager = { currentScreen = CurrentScreen.AppManager },
                        onOpenStorageAnalyzer = { currentScreen = CurrentScreen.StorageAnalyzer },
                        onOpenWebShare = { currentScreen = CurrentScreen.WebShare }
                    )
                }

                is CurrentScreen.Explorer -> {
                    FileExplorerScreen(
                        initialPath = screen.targetPath,
                        externalTabs = explorerTabs,
                        externalActiveTabIndex = activeTabIndex,
                        onTabSelect = { index -> activeTabIndex = index },
                        onCloseTab = { index ->
                            if (explorerTabs.size > 1) {
                                explorerTabs.removeAt(index)
                                if (activeTabIndex >= explorerTabs.size) {
                                    activeTabIndex = explorerTabs.size - 1
                                }
                            }
                        },
                        onNewTab = { newPath ->
                            val p = newPath ?: internalPath
                            val title = if (p == "/") "Root" else p.substringAfterLast('/').ifEmpty { "Storage" }
                            explorerTabs.add(TabInfo(title = title, path = p))
                            activeTabIndex = explorerTabs.size - 1
                        },
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToHome = { currentScreen = CurrentScreen.Home },
                        onOpenAppManager = { currentScreen = CurrentScreen.AppManager },
                        onOpenStorageAnalyzer = { currentScreen = CurrentScreen.StorageAnalyzer },
                        onOpenWebShare = { currentScreen = CurrentScreen.WebShare },
                        onOpenTextEditor = { currentScreen = CurrentScreen.TextEditor(it) },
                        onOpenHexViewer = { currentScreen = CurrentScreen.HexViewer(it) },
                        onOpenSettings = { currentScreen = CurrentScreen.Settings }
                    )
                }

                is CurrentScreen.CategoryFiles -> {
                    CategoryFilesScreen(
                        categoryType = screen.type,
                        categoryTitle = screen.title,
                        onBack = { currentScreen = CurrentScreen.Home },
                        onOpenTextEditor = { currentScreen = CurrentScreen.TextEditor(it) },
                        onOpenHexViewer = { currentScreen = CurrentScreen.HexViewer(it) }
                    )
                }

                is CurrentScreen.AppManager -> {
                    AppManagerScreen(
                        onBack = { currentScreen = CurrentScreen.Home }
                    )
                }

                is CurrentScreen.StorageAnalyzer -> {
                    StorageAnalyzerScreen(
                        onBack = { currentScreen = CurrentScreen.Home },
                        onFileClick = { file ->
                            if (file.isTextOrCode) currentScreen = CurrentScreen.TextEditor(file)
                            else currentScreen = CurrentScreen.HexViewer(file)
                        }
                    )
                }

                is CurrentScreen.WebShare -> {
                    WebShareScreen(
                        onBack = { currentScreen = CurrentScreen.Home }
                    )
                }

                is CurrentScreen.TextEditor -> {
                    TextEditorScreen(
                        fileItem = screen.file,
                        onBack = { currentScreen = CurrentScreen.Explorer() }
                    )
                }

                is CurrentScreen.HexViewer -> {
                    HexViewerScreen(
                        fileItem = screen.file,
                        onBack = { currentScreen = CurrentScreen.Explorer() }
                    )
                }

                is CurrentScreen.Settings -> {
                    SettingsScreen(
                        onNavigateBack = { currentScreen = CurrentScreen.Explorer() }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequiredScreen(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        NothingCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NOTHING FILES (2.0)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    color = NothingRed
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "STORAGE PERMISSION REQUIRED",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NothingWhite
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "To provide high-end file management, archive browsing, app inspection, and hash verification, Nothing Files requires All Files Access.",
                    fontSize = 13.sp,
                    color = NothingTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NothingRed,
                        contentColor = NothingWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "GRANT ALL FILES ACCESS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
