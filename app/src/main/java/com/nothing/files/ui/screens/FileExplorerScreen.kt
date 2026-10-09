package com.nothing.files.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.nothing.files.engine.ArchiveConfig
import com.nothing.files.engine.ArchiveEngine
import com.nothing.files.engine.BookmarkManager
import com.nothing.files.engine.FileManagerEngine
import com.nothing.files.engine.PinManager
import com.nothing.files.engine.PrivilegedFileEngine
import com.nothing.files.engine.RootShellEngine
import com.nothing.files.engine.ThumbnailEngine
import com.nothing.files.engine.SortBy
import com.nothing.files.engine.SortOption
import com.nothing.files.engine.StorageAnalyzerEngine
import com.nothing.files.engine.StorageSpaceAnalysis
import com.nothing.files.model.FileItem
import com.nothing.files.model.TabInfo
import com.nothing.files.model.ViewMode
import com.nothing.files.ui.components.AddAction
import com.nothing.files.ui.components.MiXplorerBottomBar
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.nothing.files.ui.components.FileThumbnailView
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

enum class ClipboardAction {
    COPY,
    MOVE,
    EXTRACT,
    ARCHIVE
}

data class ClipboardTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val action: ClipboardAction,
    val files: List<FileItem>,
    val archiveConfig: ArchiveConfig? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun FileExplorerScreen(
    initialPath: String? = null,
    externalTabs: SnapshotStateList<TabInfo>? = null,
    externalActiveTabIndex: Int = 0,
    onTabSelect: (Int) -> Unit = {},
    onCloseTab: (Int) -> Unit = {},
    onNewTab: (String?) -> Unit = {},
    onOpenDrawer: () -> Unit,
    onNavigateToHome: () -> Unit,
    onOpenAppManager: () -> Unit,
    onOpenStorageAnalyzer: () -> Unit,
    onOpenWebShare: () -> Unit,
    onOpenTextEditor: (FileItem) -> Unit,
    onOpenHexViewer: (FileItem) -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { FileManagerEngine() }
    val storageEngine = remember { StorageAnalyzerEngine() }
    val archiveEngine = remember { ArchiveEngine() }

    val startPath = initialPath ?: engine.defaultStoragePath

    // Multi-tab system (supports multiple browsing tabs like MiXplorer)
    val fallbackTabs = remember {
        mutableStateListOf(TabInfo(title = "Internal storage", path = startPath))
    }
    var fallbackTabIndex by remember { mutableStateOf(0) }

    val tabs = externalTabs ?: fallbackTabs
    val activeTabIndex = if (externalTabs != null) externalActiveTabIndex else fallbackTabIndex
    val setTabIndex: (Int) -> Unit = if (externalTabs != null) onTabSelect else { idx: Int -> fallbackTabIndex = idx }
    val closeTabAction: (Int) -> Unit = if (externalTabs != null) onCloseTab else { idx: Int ->
        if (fallbackTabs.size > 1) {
            fallbackTabs.removeAt(idx)
            if (fallbackTabIndex >= fallbackTabs.size) fallbackTabIndex = fallbackTabs.size - 1
        }
    }
    val newTabAction: (String?) -> Unit = if (externalTabs != null) onNewTab else { newP: String? ->
        val p = newP ?: engine.defaultStoragePath
        val title = if (p == "/") "Root" else p.substringAfterLast('/').ifEmpty { "Storage" }
        fallbackTabs.add(TabInfo(title = title, path = p))
        fallbackTabIndex = fallbackTabs.size - 1
    }

    val currentTab = tabs.getOrNull(activeTabIndex) ?: tabs.firstOrNull() ?: TabInfo(path = startPath)

    LaunchedEffect(initialPath) {
        if (initialPath != null && initialPath != currentTab.path) {
            tabs[activeTabIndex] = currentTab.navigateTo(initialPath)
        }
    }

    // Navigation & State
    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var activeViewMode by remember { mutableStateOf(ViewMode.DETAILED) }
    var activeSortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var showHidden by remember { mutableStateOf(false) }
    var foldersFirst by remember { mutableStateOf(true) }

    // Multi-Selection State (Matching Screenshots)
    val selectedFiles = remember { mutableStateListOf<FileItem>() }
    val isSelectionMode = selectedFiles.isNotEmpty()
    var showSelectionDropdown by remember { mutableStateOf(false) }
    var showSelectionOverflowMenu by remember { mutableStateOf(false) }

    // Clipboard Tasks Queue (Copy, Move, Extract - matching MiXplorer)
    val clipboardTasks = remember { mutableStateListOf<ClipboardTask>() }
    var showClipboardDropdown by remember { mutableStateOf(false) }

    // Active Dialogs
    var showSortMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var itemMenuFile by remember { mutableStateOf<FileItem?>(null) }
    var checksumFile by remember { mutableStateOf<FileItem?>(null) }
    var archiveFile by remember { mutableStateOf<FileItem?>(null) }
    var permissionsFile by remember { mutableStateOf<FileItem?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showBatchRename by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Real Action Dialogs from 3-Dot Menu & Bottom Bar
    var showCreateArchiveDialog by remember { mutableStateOf(false) }
    var showEncryptDialog by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var isFolderPickerMove by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var showExecuteDialog by remember { mutableStateOf(false) }
    var showToolsDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var filterExtension by remember { mutableStateOf("") }
    var showLockDialog by remember { mutableStateOf(false) }
    var isFolderLocked by remember { mutableStateOf(false) }
    var folderPin by remember { mutableStateOf("") }
    var showViewOptionsDialog by remember { mutableStateOf(false) }
    var showSortOptionsDialog by remember { mutableStateOf(false) }
    var showCreateDbDialog by remember { mutableStateOf(false) }
    var showCreateKeyStoreDialog by remember { mutableStateOf(false) }
    var showCreateDotFileDialog by remember { mutableStateOf(false) }
    var showCreateDocumentDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }

    // Interactive Pinch-to-Zoom Scale
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var showZoomHud by remember { mutableStateOf(false) }
    var lastZoomTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(lastZoomTime) {
        if (lastZoomTime > 0L) {
            showZoomHud = true
            delay(1200)
            showZoomHud = false
        }
    }

    // Storage summary
    var storageSummary by remember { mutableStateOf<StorageSpaceAnalysis?>(null) }
    var isRootActive by remember { mutableStateOf<Boolean>(RootShellEngine.isRootGranted()) }

    LaunchedEffect(currentTab.path) {
        if (!isRootActive) {
            isRootActive = RootShellEngine.isRootAvailable()
        }
    }

    fun refreshFiles() {
        isLoading = true
        scope.launch {
            val list = engine.listFiles(
                directoryPath = currentTab.path,
                sortOption = activeSortOption,
                showHidden = showHidden,
                foldersFirst = foldersFirst
            )
            val pinned = PinManager.getPinnedPaths(context)
            files = list.sortedWith(
                compareByDescending<FileItem> { it.path in pinned }
                    .thenByDescending { if (foldersFirst) it.isDirectory else false }
            )
            isLoading = false
        }
    }

    LaunchedEffect(currentTab.path, activeSortOption, showHidden, foldersFirst) {
        refreshFiles()
        scope.launch {
            storageSummary = storageEngine.analyzeStorage(currentTab.path)
        }
    }

    // Hardware back press handler
    BackHandler(enabled = true) {
        if (isSelectionMode) {
            selectedFiles.clear()
        } else if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
        } else if (currentTab.canGoBack) {
            tabs[activeTabIndex] = currentTab.goBack()
        } else if (currentTab.path != engine.defaultStoragePath && currentTab.path != "/") {
            val parent = File(currentTab.path).parent ?: engine.defaultStoragePath
            tabs[activeTabIndex] = currentTab.navigateTo(parent)
        } else {
            onNavigateToHome()
        }
    }

    fun executeQuickExtract(archives: List<FileItem>) {
        if (archives.isEmpty()) return
        scope.launch {
            Toast.makeText(context, "Extracting ${archives.size} archive(s)...", Toast.LENGTH_SHORT).show()
            var extractedCount = 0
            withContext(Dispatchers.IO) {
                val archiveEngine = ArchiveEngine()
                archives.forEach { archiveItem ->
                    val archiveFile = File(archiveItem.path)
                    val targetDir = File(archiveFile.parentFile ?: File(currentTab.path), archiveFile.nameWithoutExtension)
                    val ok = archiveEngine.extractArchive(archiveFile, targetDir)
                    if (ok) extractedCount++
                }
            }
            if (extractedCount > 0) {
                Toast.makeText(context, "Successfully extracted $extractedCount archive(s)", Toast.LENGTH_LONG).show()
                selectedFiles.clear()
                refreshFiles()
            } else {
                Toast.makeText(context, "Extraction failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun executeTask(task: ClipboardTask) {
        scope.launch {
            val targetDir = File(currentTab.path)
            when (task.action) {
                ClipboardAction.COPY -> {
                    val ok = engine.copyFiles(task.files.map { File(it.path) }, targetDir)
                    if (ok) {
                        Toast.makeText(context, "Copied ${task.files.size} items to ${targetDir.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Copy failed", Toast.LENGTH_SHORT).show()
                    }
                }
                ClipboardAction.MOVE -> {
                    val ok = engine.moveFiles(task.files.map { File(it.path) }, targetDir)
                    if (ok) {
                        Toast.makeText(context, "Moved ${task.files.size} items to ${targetDir.name}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Move failed", Toast.LENGTH_SHORT).show()
                    }
                }
                ClipboardAction.EXTRACT -> {
                    Toast.makeText(context, "Extracting ${task.files.size} archive(s)...", Toast.LENGTH_SHORT).show()
                    var count = 0
                    withContext(Dispatchers.IO) {
                        val archiveEngine = ArchiveEngine()
                        task.files.forEach { arcItem ->
                            val archiveFile = File(arcItem.path)
                            val outDir = if (task.files.size == 1 && targetDir.name == archiveFile.nameWithoutExtension) {
                                targetDir
                            } else {
                                File(targetDir, archiveFile.nameWithoutExtension)
                            }
                            if (archiveEngine.extractArchive(archiveFile, outDir)) {
                                count++
                            }
                        }
                    }
                    if (count > 0) {
                        Toast.makeText(context, "Extracted $count archive(s) to ${targetDir.name}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Extraction failed", Toast.LENGTH_SHORT).show()
                    }
                }
                ClipboardAction.ARCHIVE -> {
                    val config = task.archiveConfig ?: ArchiveConfig(
                        archiveName = if (task.files.size == 1) "${File(task.files.first().path).nameWithoutExtension}.zip" else "Archive_${System.currentTimeMillis() % 10000}.zip"
                    )
                    Toast.makeText(context, "Creating archive '${config.archiveName}' in ${targetDir.name}...", Toast.LENGTH_SHORT).show()
                    var ok = false
                    withContext(Dispatchers.IO) {
                        val outputFile = File(targetDir, config.archiveName)
                        val filesToArchive = task.files.map { File(it.path) }
                        ok = archiveEngine.createConfiguredArchive(
                            sourceFiles = filesToArchive,
                            outputArchiveFile = outputFile,
                            config = config
                        )
                    }
                    if (ok) {
                        Toast.makeText(context, "Archive '${config.archiveName}' created", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to create archive", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            clipboardTasks.remove(task)
            if (clipboardTasks.isEmpty()) showClipboardDropdown = false
            refreshFiles()
        }
    }

    fun executeAllTasks() {
        val tasksToRun = clipboardTasks.toList()
        scope.launch {
            val targetDir = File(currentTab.path)
            for (task in tasksToRun) {
                when (task.action) {
                    ClipboardAction.COPY -> {
                        engine.copyFiles(task.files.map { File(it.path) }, targetDir)
                    }
                    ClipboardAction.MOVE -> {
                        engine.moveFiles(task.files.map { File(it.path) }, targetDir)
                    }
                    ClipboardAction.EXTRACT -> {
                        val archiveEngine = ArchiveEngine()
                        withContext(Dispatchers.IO) {
                            task.files.forEach { arcItem ->
                                val archiveFile = File(arcItem.path)
                                val outDir = if (task.files.size == 1 && targetDir.name == archiveFile.nameWithoutExtension) {
                                    targetDir
                                } else {
                                    File(targetDir, archiveFile.nameWithoutExtension)
                                }
                                archiveEngine.extractArchive(archiveFile, outDir)
                            }
                        }
                    }
                    ClipboardAction.ARCHIVE -> {
                        val config = task.archiveConfig ?: ArchiveConfig(
                            archiveName = if (task.files.size == 1) "${File(task.files.first().path).nameWithoutExtension}.zip" else "Archive_${System.currentTimeMillis() % 10000}.zip"
                        )
                        val outputFile = File(targetDir, config.archiveName)
                        val filesToArchive = task.files.map { File(it.path) }
                        withContext(Dispatchers.IO) {
                            archiveEngine.createConfiguredArchive(
                                sourceFiles = filesToArchive,
                                outputArchiveFile = outputFile,
                                config = config
                            )
                        }
                    }
                }
            }
            clipboardTasks.clear()
            showClipboardDropdown = false
            refreshFiles()
            Toast.makeText(context, "Executed all tasks in ${targetDir.name}", Toast.LENGTH_SHORT).show()
        }
    }

    val onToggleSelect: (FileItem) -> Unit = { file ->
        if (selectedFiles.any { it.path == file.path }) {
            selectedFiles.removeAll { it.path == file.path }
        } else {
            selectedFiles.add(file)
        }
    }

    val onOpenFile: (FileItem) -> Unit = { file ->
        val archiveInfo = engine.findArchiveInfo(file.path)
        when {
            file.isDirectory || file.isArchive -> {
                // Browses directly into directory or archive as a folder!
                tabs[activeTabIndex] = currentTab.navigateTo(file.path)
            }
            archiveInfo != null && !file.isDirectory -> {
                scope.launch {
                    val tempDir = File(context.cacheDir, "archive_preview")
                    val tempFile = File(tempDir, file.name)
                    val ok = archiveEngine.extractSingleEntry(archiveInfo.first, archiveInfo.second, tempFile)
                    if (ok) {
                        val previewItem = FileItem.fromFile(tempFile)
                        if (previewItem.isTextOrCode) {
                            onOpenTextEditor(previewItem)
                        } else {
                            openWithExternalApp(previewItem, context)
                        }
                    } else {
                        Toast.makeText(context, "Cannot preview archive entry", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            file.isTextOrCode -> onOpenTextEditor(file)
            else -> openWithExternalApp(file, context)
        }
    }

    val displayFiles = files.filter { item ->
        val matchesSearch = if (searchQuery.isNotEmpty()) item.name.contains(searchQuery, ignoreCase = true) else true
        val matchesFilter = if (filterExtension.isNotEmpty()) {
            if (item.isDirectory) true else item.extension.equals(filterExtension.removePrefix("."), ignoreCase = true)
        } else true
        matchesSearch && matchesFilter
    }

    val folderCount = displayFiles.count { it.isDirectory }
    val fileCount = displayFiles.count { !it.isDirectory }

    val currentFolderName = if (currentTab.path == engine.defaultStoragePath) {
        "Internal storage"
    } else if (currentTab.path == "/") {
        "Root"
    } else {
        currentTab.path.substringAfterLast('/')
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // 1. Top Bar: Adapts to MiXplorer Selection Bar when items are selected (Screenshots 1, 3, 4)
        NothingCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            backgroundColor = NothingBlack,
            borderColor = NothingBorder
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isSelectionMode) {
                    // SELECTION ACTION BAR (Matches MiXplorer Screenshot 1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Checkmark and Count with Dropdown
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (selectedFiles.size == displayFiles.size) {
                                        selectedFiles.clear()
                                    } else {
                                        selectedFiles.clear()
                                        selectedFiles.addAll(displayFiles)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Select All",
                                    tint = NothingWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Box {
                                Column(
                                    modifier = Modifier
                                        .clickable { showSelectionDropdown = true }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${selectedFiles.size}",
                                            fontFamily = FontFamily.Default,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = NothingWhite
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = NothingWhite,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    val totalSelectedBytes = selectedFiles.sumOf { it.size }
                                    Text(
                                        text = formatBytes(totalSelectedBytes),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = NothingTextSecondary
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSelectionDropdown,
                                    onDismissRequest = { showSelectionDropdown = false },
                                    modifier = Modifier
                                        .background(NothingBlack)
                                        .border(1.dp, NothingBorder, RoundedCornerShape(12.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Select all", fontFamily = FontFamily.Monospace, color = NothingWhite) },
                                        colors = MenuDefaults.itemColors(
                                            textColor = NothingWhite,
                                            leadingIconColor = NothingWhite
                                        ),
                                        onClick = {
                                            showSelectionDropdown = false
                                            selectedFiles.clear()
                                            selectedFiles.addAll(displayFiles)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Invert selection", fontFamily = FontFamily.Monospace, color = NothingWhite) },
                                        colors = MenuDefaults.itemColors(
                                            textColor = NothingWhite,
                                            leadingIconColor = NothingWhite
                                        ),
                                        onClick = {
                                            showSelectionDropdown = false
                                            val remaining = displayFiles.filter { it !in selectedFiles }
                                            selectedFiles.clear()
                                            selectedFiles.addAll(remaining)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Clear selection", fontFamily = FontFamily.Monospace, color = NothingWhite) },
                                        colors = MenuDefaults.itemColors(
                                            textColor = NothingWhite,
                                            leadingIconColor = NothingWhite
                                        ),
                                        onClick = {
                                            showSelectionDropdown = false
                                            selectedFiles.clear()
                                        }
                                    )
                                }
                            }
                        }

                        // Right Action Buttons: Delete, Copy, Cut, Rename, Info, Overflow
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 1. Delete
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NothingWhite, modifier = Modifier.size(20.dp))
                            }

                            // 2. Copy
                            IconButton(
                                onClick = {
                                    val count = selectedFiles.size
                                    clipboardTasks.add(ClipboardTask(action = ClipboardAction.COPY, files = selectedFiles.toList()))
                                    selectedFiles.clear()
                                    Toast.makeText(context, "Copied $count items to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NothingWhite, modifier = Modifier.size(20.dp))
                            }

                            // 3. Cut
                            IconButton(
                                onClick = {
                                    val count = selectedFiles.size
                                    clipboardTasks.add(ClipboardTask(action = ClipboardAction.MOVE, files = selectedFiles.toList()))
                                    selectedFiles.clear()
                                    Toast.makeText(context, "Cut $count items to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = "Cut", tint = NothingWhite, modifier = Modifier.size(20.dp))
                            }

                            // 4. Rename (Letter A - matching MiXplorer Screenshot 1 & 4)
                            IconButton(
                                onClick = { showRenameDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = "A",
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = NothingWhite
                                )
                            }

                            // 5. Info (Screenshot 2)
                            IconButton(
                                onClick = { showInfoDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = NothingWhite, modifier = Modifier.size(20.dp))
                            }

                            // Clipboard Button (Active when tasks are queued, matching MiXplorer)
                            if (clipboardTasks.isNotEmpty()) {
                                Box {
                                    IconButton(
                                        onClick = { showClipboardDropdown = !showClipboardDropdown },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Assignment,
                                                contentDescription = "Clipboard Tasks",
                                                tint = NothingWhite,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Canvas(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .align(Alignment.TopEnd)
                                            ) {
                                                drawCircle(color = LedOnRed)
                                            }
                                        }
                                    }

                                    ClipboardTasksDropdown(
                                        expanded = showClipboardDropdown,
                                        onDismissRequest = { showClipboardDropdown = false },
                                        tasks = clipboardTasks,
                                        currentFolderName = currentFolderName,
                                        onExecuteTask = { task ->
                                            selectedFiles.clear()
                                            executeTask(task)
                                        },
                                        onExecuteAll = {
                                            selectedFiles.clear()
                                            executeAllTasks()
                                        },
                                        onClear = {
                                            clipboardTasks.clear()
                                            showClipboardDropdown = false
                                            Toast.makeText(context, "Cleared clipboard tasks", Toast.LENGTH_SHORT).show()
                                        },
                                        onRemoveTask = { task ->
                                            clipboardTasks.remove(task)
                                            if (clipboardTasks.isEmpty()) showClipboardDropdown = false
                                        }
                                    )
                                }
                            }

                            // 6. Overflow Menu (Screenshot 3 - All 14 Real Working Actions)
                            Box {
                                IconButton(
                                    onClick = { showSelectionOverflowMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = NothingWhite, modifier = Modifier.size(20.dp))
                                }

                                DropdownMenu(
                                    expanded = showSelectionOverflowMenu,
                                    onDismissRequest = { showSelectionOverflowMenu = false },
                                    modifier = Modifier
                                        .background(NothingBlack)
                                        .border(1.dp, NothingBorder, RoundedCornerShape(12.dp))
                                ) {
                                    val menuColors = MenuDefaults.itemColors(
                                        textColor = NothingWhite,
                                        leadingIconColor = NothingWhite
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            shareFiles(context, selectedFiles)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Send to...", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            shareFiles(context, selectedFiles)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            selectedFiles.firstOrNull()?.let { openWithExternalApp(it, context) }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open with...", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            selectedFiles.firstOrNull()?.let { openWithExternalApp(it, context) }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Archive", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            showCreateArchiveDialog = true
                                        }
                                    )
                                    val selectedArchives = selectedFiles.filter { it.isArchive }
                                    if (selectedArchives.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Archive, contentDescription = null, tint = NothingRed, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Extract...", fontFamily = FontFamily.Default, color = NothingWhite)
                                                }
                                            },
                                            colors = menuColors,
                                            onClick = {
                                                showSelectionOverflowMenu = false
                                                val count = selectedArchives.size
                                                clipboardTasks.add(ClipboardTask(action = ClipboardAction.EXTRACT, files = selectedArchives.toList()))
                                                selectedFiles.clear()
                                                Toast.makeText(context, "Queued $count archive(s) for extraction", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Archive, contentDescription = null, tint = NothingRed, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Extract here", fontFamily = FontFamily.Default, color = NothingWhite)
                                                }
                                            },
                                            colors = menuColors,
                                            onClick = {
                                                showSelectionOverflowMenu = false
                                                executeQuickExtract(selectedArchives)
                                            }
                                        )
                                        if (selectedArchives.size == 1) {
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Archive, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Browse archive", fontFamily = FontFamily.Default, color = NothingWhite)
                                                    }
                                                },
                                                colors = menuColors,
                                                onClick = {
                                                    showSelectionOverflowMenu = false
                                                    val arc = selectedArchives.first()
                                                    selectedFiles.clear()
                                                    tabs[activeTabIndex] = currentTab.navigateTo(arc.path)
                                                }
                                            )
                                        }
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Encrypt", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            showEncryptDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add to...", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            val first = selectedFiles.firstOrNull()
                                            if (first != null) {
                                                val added = BookmarkManager.addBookmark(context, first.name, first.path)
                                                Toast.makeText(context, if (added) "Added '${first.name}' to Bookmarks Drawer" else "Already in Bookmarks", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Pin", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            selectedFiles.forEach { PinManager.togglePin(context, it.path) }
                                            val isPinnedNow = selectedFiles.firstOrNull()?.let { PinManager.isPinned(context, it.path) } ?: false
                                            Toast.makeText(context, if (isPinnedNow) "Pinned to top" else "Unpinned", Toast.LENGTH_SHORT).show()
                                            selectedFiles.clear()
                                            refreshFiles()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Hide", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            scope.launch {
                                                selectedFiles.forEach { file ->
                                                    val f = File(file.path)
                                                    val newName = if (f.name.startsWith(".")) f.name.removePrefix(".") else ".${f.name}"
                                                    engine.renameFile(f, newName)
                                                }
                                                selectedFiles.clear()
                                                refreshFiles()
                                                Toast.makeText(context, "Toggled hidden status", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Copy to...", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            isFolderPickerMove = false
                                            showFolderPicker = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Move to...", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            isFolderPickerMove = true
                                            showFolderPicker = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Tags", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            showTagsDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share local link", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            val first = selectedFiles.firstOrNull()
                                            val path = first?.path ?: ""
                                            val ip = getLocalIpAddress(context)
                                            val link = "http://$ip:8080/files${Uri.encode(path)}"
                                            copyToClipboard(context, link, "Web Share Link")
                                            Toast.makeText(context, "Local Web Share URL copied to clipboard:\n$link", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Mail to...", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            val first = selectedFiles.firstOrNull()
                                            try {
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "message/rfc822"
                                                    putExtra(Intent.EXTRA_SUBJECT, "Sharing: ${first?.name ?: ""}")
                                                    if (first != null) {
                                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(first.path))
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Send Email via..."))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Copy path", fontFamily = FontFamily.Default, color = NothingWhite) },
                                        colors = menuColors,
                                        onClick = {
                                            showSelectionOverflowMenu = false
                                            val paths = selectedFiles.joinToString("\n") { it.path }
                                            copyToClipboard(context, paths, "File Paths")
                                            Toast.makeText(context, "Paths copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // NORMAL TOP BAR (Drawer, Folder name, More menu)
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
                                        text = currentFolderName,
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
                                    text = "♥ $folderCount folders, $fileCount files",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = NothingTextSecondary
                                )
                            }
                        }

                        // Clipboard tasks button right next to 3-dot menu (Matching MiXplorer Screenshot 1 & 2)
                        if (clipboardTasks.isNotEmpty()) {
                            Box {
                                IconButton(
                                    onClick = { showClipboardDropdown = !showClipboardDropdown },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = "Clipboard Tasks",
                                            tint = NothingWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Canvas(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .align(Alignment.TopEnd)
                                        ) {
                                            drawCircle(color = LedOnRed)
                                        }
                                    }
                                }

                                ClipboardTasksDropdown(
                                    expanded = showClipboardDropdown,
                                    onDismissRequest = { showClipboardDropdown = false },
                                    tasks = clipboardTasks,
                                    currentFolderName = currentFolderName,
                                    onExecuteTask = { executeTask(it) },
                                    onExecuteAll = { executeAllTasks() },
                                    onClear = {
                                        clipboardTasks.clear()
                                        showClipboardDropdown = false
                                        Toast.makeText(context, "Cleared clipboard tasks", Toast.LENGTH_SHORT).show()
                                    },
                                    onRemoveTask = {
                                        clipboardTasks.remove(it)
                                        if (clipboardTasks.isEmpty()) showClipboardDropdown = false
                                    }
                                )
                            }
                        }

                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = NothingWhite)
                            }

                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                modifier = Modifier
                                    .background(NothingBlack)
                                    .border(1.dp, NothingBorder, RoundedCornerShape(12.dp))
                            ) {
                                val moreMenuColors = MenuDefaults.itemColors(
                                    textColor = NothingWhite,
                                    leadingIconColor = NothingWhite
                                )
                                DropdownMenuItem(
                                    text = { Text("Undo", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    colors = moreMenuColors,
                                    onClick = {
                                        showMoreMenu = false
                                        Toast.makeText(context, "No recent operation to undo", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isFolderLocked) "Unlock" else "Lock", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        showLockDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Tools", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        showToolsDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Filter", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        showFilterDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Servers", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenWebShare()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Execute", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        showExecuteDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Open in terminal", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        try {
                                            val termuxIntent = Intent("com.termux.app.RunCommandService")
                                            termuxIntent.setPackage("com.termux")
                                            termuxIntent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
                                            termuxIntent.putExtra("com.termux.RUN_COMMAND_WORKDIR", currentTab.path)
                                            context.startService(termuxIntent)
                                        } catch (_: Exception) {
                                            showExecuteDialog = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        onOpenSettings()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Exit", fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite) },
                                    onClick = {
                                        showMoreMenu = false
                                        (context as? android.app.Activity)?.finish()
                                    }
                                )
                            }
                        }
                    }
                }

                // Tab Strip (MiXplorer multi-tab strip: Home | Tab 1 [✕] | Tab 2 [✕] ... [+])
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Home",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = NothingTextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onNavigateToHome() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(1.dp)
                            .background(NothingBorder)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    tabs.forEachIndexed { index, tab ->
                        val isActive = index == activeTabIndex
                        val tabName = if (tab.path == engine.defaultStoragePath) {
                            "Internal storage"
                        } else if (tab.path == "/") {
                            "Root"
                        } else {
                            tab.title.ifEmpty { tab.path.substringAfterLast('/') }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { setTabIndex(index) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tabName,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isActive) NothingWhite else NothingTextSecondary,
                                        maxLines = 1
                                    )

                                    if (tabs.size > 1) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close Tab",
                                            tint = if (isActive) NothingWhite.copy(alpha = 0.7f) else NothingTextSecondary,
                                            modifier = Modifier
                                                .size(13.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .clickable { closeTabAction(index) }
                                        )
                                    }
                                }

                                if (isActive) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(2.dp)
                                            .background(NothingRed)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    IconButton(
                        onClick = { newTabAction(null) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Tab",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Storage usage line under tabs (Screenshot 1: < ...1 GiB / 227.04 GiB 28%  ...)
                val used = storageSummary?.formattedUsed ?: "63.1 GiB"
                val total = storageSummary?.formattedTotal ?: "227 GiB"
                val percent = storageSummary?.usedPercent ?: 28

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (currentTab.canGoBack) {
                                    tabs[activeTabIndex] = currentTab.goBack()
                                } else if (currentTab.path != engine.defaultStoragePath) {
                                    val p = File(currentTab.path).parent
                                    if (p != null) tabs[activeTabIndex] = currentTab.navigateTo(p)
                                } else {
                                    onNavigateToHome()
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "$used / $total   $percent%",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NothingTextSecondary
                        )
                    }

                    IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Search Box when toggled
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter files in current folder...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NothingRed,
                            unfocusedBorderColor = NothingBorder,
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite,
                            cursorColor = NothingRed
                        )
                    )
                }
            }
        }

        // 2. File Content Area (With Dynamic Pinch Zoom & Thumbnails)
        Box(
            modifier = Modifier
                .weight(1f)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val downPointers = event.changes.filter { it.pressed }
                            if (downPointers.size >= 2) {
                                val p0 = downPointers[0].position
                                val p1 = downPointers[1].position
                                val prevP0 = downPointers[0].previousPosition
                                val prevP1 = downPointers[1].previousPosition
                                val currDist = (p0 - p1).getDistance()
                                val prevDist = (prevP0 - prevP1).getDistance()
                                if (prevDist > 1f && currDist > 1f) {
                                    val factor = currDist / prevDist
                                    if (factor in 0.3f..3.0f) {
                                        zoomScale = (zoomScale * factor).coerceIn(0.35f, 4.0f)
                                        lastZoomTime = System.currentTimeMillis()
                                    }
                                }
                                event.changes.forEach { it.consume() }
                            } else {
                                if (event.changes.all { !it.pressed }) {
                                    break
                                }
                            }
                        }
                    }
                }
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
                }
            } else if (displayFiles.isEmpty()) {
                val isAndroidDataOrObb = currentTab.path.lowercase().contains("/android/data") || currentTab.path.lowercase().contains("/android/obb")
                val hasPrivilegedAccess = isRootActive || RootShellEngine.isRootGranted() || PrivilegedFileEngine.isShizukuPermissionGranted()
                val isRestricted = isAndroidDataOrObb && !hasPrivilegedAccess
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = when {
                                searchQuery.isNotEmpty() -> "NO MATCHING FILES"
                                isRestricted -> "FOLDER RESTRICTED"
                                else -> "FOLDER IS EMPTY"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isRestricted) {
                            Text(
                                text = "Restricted by Android Scoped Storage. Grant Root or Shizuku permission to browse and modify files.",
                                fontFamily = FontFamily.Default,
                                fontSize = 11.sp,
                                color = NothingTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NothingRed)
                                        .clickable {
                                            scope.launch {
                                                isRootActive = RootShellEngine.isRootAvailable(forceCheck = true)
                                                refreshFiles()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("GRANT ROOT", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NothingWhite)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NothingSurfaceElevated)
                                        .border(1.dp, NothingBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            PrivilegedFileEngine.requestShizukuPermission()
                                            refreshFiles()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("GRANT SHIZUKU", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NothingWhite)
                                }
                            }
                        } else {
                            Text(
                                text = when {
                                    searchQuery.isNotEmpty() -> "Try a different search query"
                                    isAndroidDataOrObb -> "Accessible via ${if (isRootActive || RootShellEngine.isRootGranted()) "Root (KernelSU)" else "Shizuku"}. Folder is empty."
                                    else -> "No items in this directory"
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = NothingTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            if (!showHidden) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tip: Enable 'Show hidden files' in 3-dot menu to view .nomedia",
                                    fontFamily = FontFamily.Default,
                                    fontSize = 10.sp,
                                    color = NothingTextSecondary.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                when (activeViewMode) {
                    ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = (85.dp * zoomScale).coerceIn(38.dp, 400.dp)),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy((6 * zoomScale).coerceIn(2f, 24f).dp),
                            horizontalArrangement = Arrangement.spacedBy((6 * zoomScale).coerceIn(2f, 24f).dp)
                        ) {
                            items(displayFiles, key = { it.path }) { file ->
                                val isSelected = selectedFiles.any { it.path == file.path }
                                val isPinned = PinManager.isPinned(context, file.path)
                                FileExplorerGridItem(
                                    file = file,
                                    zoomScale = zoomScale,
                                    isPinned = isPinned,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onOpen = { onOpenFile(file) },
                                    onToggleSelect = { onToggleSelect(file) }
                                )
                            }
                        }
                    }

                    ViewMode.GALLERY -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = (130.dp * zoomScale).coerceIn(60.dp, 500.dp)),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy((8 * zoomScale).coerceIn(2f, 28f).dp),
                            horizontalArrangement = Arrangement.spacedBy((8 * zoomScale).coerceIn(2f, 28f).dp)
                        ) {
                            items(displayFiles, key = { it.path }) { file ->
                                val isSelected = selectedFiles.any { it.path == file.path }
                                val isPinned = PinManager.isPinned(context, file.path)
                                FileExplorerGalleryItem(
                                    file = file,
                                    zoomScale = zoomScale,
                                    isPinned = isPinned,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onOpen = { onOpenFile(file) },
                                    onToggleSelect = { onToggleSelect(file) }
                                )
                            }
                        }
                    }

                    ViewMode.COLUMNED -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = (130.dp * zoomScale).coerceIn(60.dp, 500.dp)),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy((4 * zoomScale).coerceIn(1f, 16f).dp),
                            horizontalArrangement = Arrangement.spacedBy((4 * zoomScale).coerceIn(1f, 16f).dp)
                        ) {
                            items(displayFiles, key = { it.path }) { file ->
                                val isSelected = selectedFiles.any { it.path == file.path }
                                val isPinned = PinManager.isPinned(context, file.path)
                                FileExplorerCompactRow(
                                    file = file,
                                    zoomScale = zoomScale,
                                    isPinned = isPinned,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onOpen = { onOpenFile(file) },
                                    onToggleSelect = { onToggleSelect(file) }
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(((if (activeViewMode == ViewMode.COMPACT) 2f else 4f) * zoomScale).coerceIn(1f, 16f).dp)
                        ) {
                            items(displayFiles, key = { it.path }) { file ->
                                val isSelected = selectedFiles.any { it.path == file.path }
                                val isPinned = PinManager.isPinned(context, file.path)
                                val fileTag = context.getSharedPreferences("nothing_files_tags", Context.MODE_PRIVATE).getString(file.path, null)

                                FileExplorerRowItem(
                                    file = file,
                                    viewMode = activeViewMode,
                                    zoomScale = zoomScale,
                                    isPinned = isPinned,
                                    tag = fileTag,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onOpen = { onOpenFile(file) },
                                    onToggleSelect = { onToggleSelect(file) },
                                    onMenuClick = { itemMenuFile = file }
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Live Floating Zoom Indicator (Shows smooth real-time percentage during pinch; no fixed presets)
            if (showZoomHud) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NothingBlack)
                            .border(1.dp, NothingBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(color = LedOnRed)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NothingWhite
                            )
                        }
                    }
                }
            }
        }

        // 3. Persistent Bottom Action Bar (Screenshots 1 & 3)
        MiXplorerBottomBar(
            onSearchClick = { isSearchActive = !isSearchActive },
            activeViewMode = activeViewMode,
            onSelectViewMode = { activeViewMode = it },
            onViewOptionsClick = { showViewOptionsDialog = true },
            onAddAction = { action ->
                when (action) {
                    AddAction.FOLDER -> showCreateFolderDialog = true
                    AddAction.FILE -> showCreateFileDialog = true
                    AddAction.DATABASE -> showCreateDbDialog = true
                    AddAction.ENCRYPTED -> showEncryptDialog = true
                    AddAction.KEYSTORE -> showCreateKeyStoreDialog = true
                    AddAction.SHORTCUT -> {
                        val added = BookmarkManager.addBookmark(context, currentFolderName, currentTab.path)
                        Toast.makeText(context, if (added) "Added '$currentFolderName' to Bookmarks Drawer" else "Already in Bookmarks", Toast.LENGTH_SHORT).show()
                    }
                    AddAction.DOT_FILE -> showCreateDotFileDialog = true
                    AddAction.DOCUMENT -> showCreateDocumentDialog = true
                    AddAction.STORAGE -> showStorageDialog = true
                }
            },
            onRefreshClick = { refreshFiles() },
            activeSortOption = activeSortOption,
            onSelectSortOption = { activeSortOption = it },
            onSortOptionsClick = { showSortOptionsDialog = true },
            onSelectAllClick = {
                if (selectedFiles.isEmpty()) {
                    selectedFiles.addAll(displayFiles)
                } else {
                    selectedFiles.clear()
                }
            }
        )

        // Single File Item Options Menu
        itemMenuFile?.let { item ->
            DropdownMenu(
                expanded = true,
                onDismissRequest = { itemMenuFile = null },
                modifier = Modifier
                    .background(NothingBlack)
                    .border(1.dp, NothingBorder, RoundedCornerShape(12.dp))
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy", fontFamily = FontFamily.Monospace)
                        }
                    },
                    onClick = {
                        itemMenuFile = null
                        clipboardTasks.add(ClipboardTask(action = ClipboardAction.COPY, files = listOf(item)))
                        Toast.makeText(context, "Added '${item.name}' to Clipboard (Copy)", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cut / Move", fontFamily = FontFamily.Monospace)
                        }
                    },
                    onClick = {
                        itemMenuFile = null
                        clipboardTasks.add(ClipboardTask(action = ClipboardAction.MOVE, files = listOf(item)))
                        Toast.makeText(context, "Added '${item.name}' to Clipboard (Move)", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Rename", fontFamily = FontFamily.Monospace) },
                    onClick = {
                        itemMenuFile = null
                        selectedFiles.clear()
                        selectedFiles.add(item)
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Properties / Info", fontFamily = FontFamily.Monospace) },
                    onClick = {
                        itemMenuFile = null
                        selectedFiles.clear()
                        selectedFiles.add(item)
                        showInfoDialog = true
                    }
                )
                if (item.isTextOrCode) {
                    DropdownMenuItem(
                        text = { Text("Code / Text Editor", fontFamily = FontFamily.Monospace) },
                        onClick = {
                            itemMenuFile = null
                            onOpenTextEditor(item)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Hex Viewer", fontFamily = FontFamily.Monospace) },
                    onClick = {
                        itemMenuFile = null
                        onOpenHexViewer(item)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Checksum / Hash (MD5, SHA)", fontFamily = FontFamily.Monospace) },
                    onClick = {
                        checksumFile = item
                        itemMenuFile = null
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Archive...", fontFamily = FontFamily.Monospace)
                        }
                    },
                    onClick = {
                        val file = item
                        itemMenuFile = null
                        selectedFiles.clear()
                        selectedFiles.add(file)
                        showCreateArchiveDialog = true
                    }
                )
                if (item.isArchive) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Browse Inside Archive", fontFamily = FontFamily.Monospace)
                            }
                        },
                        onClick = {
                            itemMenuFile = null
                            tabs[activeTabIndex] = currentTab.navigateTo(item.path)
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = NothingRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Extract (Add to clipboard)", fontFamily = FontFamily.Monospace)
                            }
                        },
                        onClick = {
                            itemMenuFile = null
                            clipboardTasks.add(ClipboardTask(action = ClipboardAction.EXTRACT, files = listOf(item)))
                            Toast.makeText(context, "Added '${item.name}' to Clipboard (Extract)", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = NothingWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Extract here", fontFamily = FontFamily.Monospace)
                            }
                        },
                        onClick = {
                            itemMenuFile = null
                            executeQuickExtract(listOf(item))
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Permissions (chmod)", fontFamily = FontFamily.Monospace) },
                    onClick = {
                        permissionsFile = item
                        itemMenuFile = null
                    }
                )
                DropdownMenuItem(
                    text = { Text("Open in External App", fontFamily = FontFamily.Monospace) },
                    onClick = {
                        openWithExternalApp(item, context)
                        itemMenuFile = null
                    }
                )
            }
        }

        // 1. Root Shell Executor Dialog
        if (showExecuteDialog) {
            ExecuteCommandDialog(
                initialDirectory = currentTab.path,
                onDismiss = { showExecuteDialog = false }
            )
        }

        // 2. Tools Menu Dialog
        if (showToolsDialog) {
            AlertDialog(
                onDismissRequest = { showToolsDialog = false },
                containerColor = NothingSurface,
                title = {
                    Text("TOOLS & UTILITIES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showToolsDialog = false
                                onOpenAppManager()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = NothingRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("App Manager (APK Extractor)", color = NothingWhite, fontFamily = FontFamily.Default)
                            }
                        }
                        TextButton(
                            onClick = {
                                showToolsDialog = false
                                onOpenStorageAnalyzer()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.PieChart, contentDescription = null, tint = NothingRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Storage Space Analyzer", color = NothingWhite, fontFamily = FontFamily.Default)
                            }
                        }
                        TextButton(
                            onClick = {
                                showToolsDialog = false
                                onOpenWebShare()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Wifi, contentDescription = null, tint = NothingRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Web Share HTTP Server", color = NothingWhite, fontFamily = FontFamily.Default)
                            }
                        }
                        TextButton(
                            onClick = {
                                showToolsDialog = false
                                showExecuteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Android, contentDescription = null, tint = NothingRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("SU Root Shell Console", color = NothingWhite, fontFamily = FontFamily.Default)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showToolsDialog = false }) {
                        Text("CLOSE", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 3. Filter Files Dialog
        if (showFilterDialog) {
            var tempExt by remember { mutableStateOf(filterExtension) }
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                containerColor = NothingSurface,
                title = { Text("FILTER FILES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Filter by file extension or type:", fontSize = 12.sp, color = NothingTextSecondary)
                        OutlinedTextField(
                            value = tempExt,
                            onValueChange = { tempExt = it },
                            placeholder = { Text("e.g. apk, mp4, pdf, jpg") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            ),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("apk", "mp4", "jpg", "pdf", "zip").forEach { ext ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NothingSurfaceElevated)
                                        .clickable { tempExt = ext }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(ext.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = NothingRed)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            filterExtension = tempExt.trim()
                            showFilterDialog = false
                        }
                    ) {
                        Text("APPLY", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            filterExtension = ""
                            showFilterDialog = false
                        }
                    ) {
                        Text("CLEAR", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 4. Folder Lock PIN Dialog
        if (showLockDialog) {
            var enteredPin by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showLockDialog = false },
                containerColor = NothingSurface,
                title = {
                    Text(
                        text = if (isFolderLocked) "UNLOCK FOLDER" else "LOCK FOLDER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (isFolderLocked) "Enter your PIN to unlock this directory:" else "Set a 4-digit PIN to lock access to this directory:",
                            fontSize = 12.sp,
                            color = NothingTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { if (it.length <= 4) enteredPin = it },
                            label = { Text("4-Digit PIN") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (isFolderLocked) {
                                if (enteredPin == folderPin) {
                                    isFolderLocked = false
                                    showLockDialog = false
                                    Toast.makeText(context, "Folder unlocked", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (enteredPin.length == 4) {
                                    folderPin = enteredPin
                                    isFolderLocked = true
                                    showLockDialog = false
                                    Toast.makeText(context, "Folder locked with PIN", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("CONFIRM", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLockDialog = false }) {
                        Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 5. Create SQLite DB Dialog
        if (showCreateDbDialog) {
            var dbName by remember { mutableStateOf("database.db") }
            AlertDialog(
                onDismissRequest = { showCreateDbDialog = false },
                containerColor = NothingSurface,
                title = { Text("NEW SQLITE DATABASE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
                text = {
                    OutlinedTextField(
                        value = dbName,
                        onValueChange = { dbName = it },
                        label = { Text("Database Name (.db)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NothingRed,
                            unfocusedBorderColor = NothingBorder,
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val finalName = if (dbName.endsWith(".db")) dbName else "$dbName.db"
                            scope.launch {
                                val f = File(currentTab.path, finalName)
                                f.createNewFile()
                                showCreateDbDialog = false
                                refreshFiles()
                                Toast.makeText(context, "Created SQLite database: $finalName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("CREATE", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDbDialog = false }) {
                        Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 6. Create KeyStore Dialog
        if (showCreateKeyStoreDialog) {
            var ksName by remember { mutableStateOf("security.keystore") }
            AlertDialog(
                onDismissRequest = { showCreateKeyStoreDialog = false },
                containerColor = NothingSurface,
                title = { Text("GENERATE KEYSTORE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
                text = {
                    OutlinedTextField(
                        value = ksName,
                        onValueChange = { ksName = it },
                        label = { Text("KeyStore Name (.keystore / .jks)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NothingRed,
                            unfocusedBorderColor = NothingBorder,
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val finalName = if (ksName.contains(".")) ksName else "$ksName.keystore"
                            scope.launch {
                                val f = File(currentTab.path, finalName)
                                f.createNewFile()
                                showCreateKeyStoreDialog = false
                                refreshFiles()
                                Toast.makeText(context, "Created KeyStore: $finalName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("GENERATE", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateKeyStoreDialog = false }) {
                        Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 7. Create Dot File Dialog
        if (showCreateDotFileDialog) {
            var dotName by remember { mutableStateOf(".nomedia") }
            AlertDialog(
                onDismissRequest = { showCreateDotFileDialog = false },
                containerColor = NothingSurface,
                title = { Text("CREATE DOT FILE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = dotName,
                            onValueChange = { dotName = it },
                            label = { Text("Dot file name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(".nomedia", ".gitkeep", ".gitignore", ".env").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NothingSurfaceElevated)
                                        .clickable { dotName = preset }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(preset, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingWhite)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val finalName = if (dotName.startsWith(".")) dotName else ".$dotName"
                            scope.launch {
                                val f = File(currentTab.path, finalName)
                                f.createNewFile()
                                showCreateDotFileDialog = false
                                showHidden = true
                                refreshFiles()
                                Toast.makeText(context, "Created $finalName", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("CREATE", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDotFileDialog = false }) {
                        Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 8. Create Document Dialog
        if (showCreateDocumentDialog) {
            var docName by remember { mutableStateOf("notes.txt") }
            AlertDialog(
                onDismissRequest = { showCreateDocumentDialog = false },
                containerColor = NothingSurface,
                title = { Text("NEW DOCUMENT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = docName,
                            onValueChange = { docName = it },
                            label = { Text("Document Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("notes.txt", "README.md", "script.sh", "data.json").forEach { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(NothingSurfaceElevated)
                                        .clickable { docName = preset }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(preset, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingWhite)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val finalName = if (docName.contains(".")) docName else "$docName.txt"
                            scope.launch {
                                val f = File(currentTab.path, finalName)
                                f.createNewFile()
                                showCreateDocumentDialog = false
                                refreshFiles()
                                onOpenTextEditor(FileItem.fromFile(f))
                            }
                        }
                    ) {
                        Text("CREATE & EDIT", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDocumentDialog = false }) {
                        Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 9. Storage Locations Dialog
        if (showStorageDialog) {
            AlertDialog(
                onDismissRequest = { showStorageDialog = false },
                containerColor = NothingSurface,
                title = { Text("STORAGE LOCATIONS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showStorageDialog = false
                                tabs[activeTabIndex] = currentTab.navigateTo(engine.defaultStoragePath)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Internal Storage (/storage/emulated/0)", color = NothingWhite, fontFamily = FontFamily.Default)
                        }
                        TextButton(
                            onClick = {
                                showStorageDialog = false
                                tabs[activeTabIndex] = currentTab.navigateTo("/")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Root Filesystem (/) (SU Root)", color = NothingRed, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = {
                                showStorageDialog = false
                                val dcim = File(engine.defaultStoragePath, "DCIM").absolutePath
                                tabs[activeTabIndex] = currentTab.navigateTo(dcim)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("DCIM Camera Storage", color = NothingWhite, fontFamily = FontFamily.Default)
                        }
                        TextButton(
                            onClick = {
                                showStorageDialog = false
                                val dl = File(engine.defaultStoragePath, "Download").absolutePath
                                tabs[activeTabIndex] = currentTab.navigateTo(dl)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Downloads Directory", color = NothingWhite, fontFamily = FontFamily.Default)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStorageDialog = false }) {
                        Text("CLOSE", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 10. View Options Dialog
        if (showViewOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showViewOptionsDialog = false },
                containerColor = NothingSurface,
                title = { Text("VIEW OPTIONS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Show hidden files (.)", color = NothingWhite, fontSize = 13.sp)
                            androidx.compose.material3.Switch(
                                checked = showHidden,
                                onCheckedChange = { showHidden = it }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Keep folders on top", color = NothingWhite, fontSize = 13.sp)
                            androidx.compose.material3.Switch(
                                checked = foldersFirst,
                                onCheckedChange = { foldersFirst = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showViewOptionsDialog = false }) {
                        Text("DONE", color = NothingRed, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }

        // 11. Sort Options Dialog
        if (showSortOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showSortOptionsDialog = false },
                containerColor = NothingSurface,
                title = { Text("SORT OPTIONS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Folders first", color = NothingWhite, fontSize = 13.sp)
                            androidx.compose.material3.Switch(
                                checked = foldersFirst,
                                onCheckedChange = { foldersFirst = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSortOptionsDialog = false }) {
                        Text("DONE", color = NothingRed, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }

    // Rename Dialog (Screenshot 4)
    if (showRenameDialog && selectedFiles.isNotEmpty()) {
        RenameDialog(
            targetFiles = selectedFiles.toList(),
            onDismiss = { showRenameDialog = false },
            onRenamed = {
                selectedFiles.clear()
                refreshFiles()
            }
        )
    }

    // File Info Dialog (Screenshot 2)
    if (showInfoDialog && selectedFiles.isNotEmpty()) {
        FileInfoDialog(
            targetFiles = selectedFiles.toList(),
            onDismiss = { showInfoDialog = false },
            onOpenChecksum = { checksumFile = it },
            onOpenPermissions = { permissionsFile = it }
        )
    }

    // Real Archive Dialog
    if (showCreateArchiveDialog && selectedFiles.isNotEmpty()) {
        CreateArchiveDialog(
            targetFiles = selectedFiles.toList(),
            currentDirectory = currentTab.path,
            onDismiss = { showCreateArchiveDialog = false },
            onQueueToClipboard = { config ->
                val files = selectedFiles.toList()
                val count = files.size
                clipboardTasks.add(
                    ClipboardTask(
                        action = ClipboardAction.ARCHIVE,
                        files = files,
                        archiveConfig = config
                    )
                )
                selectedFiles.clear()
                showCreateArchiveDialog = false
                Toast.makeText(context, "Queued archive '${config.archiveName}' ($count items) to clipboard", Toast.LENGTH_SHORT).show()
            }
        )
    }


    // Real Encrypt / Decrypt Dialog
    if (showEncryptDialog && selectedFiles.isNotEmpty()) {
        EncryptDialog(
            targetFiles = selectedFiles.toList(),
            onDismiss = { showEncryptDialog = false },
            onSuccess = {
                selectedFiles.clear()
                refreshFiles()
            }
        )
    }

    // Real Folder Picker Dialog (Copy to / Move to)
    if (showFolderPicker && selectedFiles.isNotEmpty()) {
        FolderPickerDialog(
            title = if (isFolderPickerMove) "Move to folder" else "Copy to folder",
            targetFiles = selectedFiles.toList(),
            isMove = isFolderPickerMove,
            onDismiss = { showFolderPicker = false },
            onComplete = {
                selectedFiles.clear()
                refreshFiles()
            }
        )
    }

    // Real Tags Dialog
    if (showTagsDialog && selectedFiles.isNotEmpty()) {
        TagsDialog(
            targetFiles = selectedFiles.toList(),
            onDismiss = { showTagsDialog = false },
            onTagsUpdated = {
                refreshFiles()
            }
        )
    }

    // Secondary Dialogs
    checksumFile?.let { file ->
        ChecksumDialog(fileItem = file, onDismiss = { checksumFile = null })
    }

    archiveFile?.let { file ->
        ArchiveViewerDialog(archiveFile = file, onDismiss = { archiveFile = null; refreshFiles() })
    }

    permissionsFile?.let { file ->
        PermissionsDialog(fileItem = file, onDismiss = { permissionsFile = null; refreshFiles() })
    }

    if (showBatchRename) {
        BatchRenameDialog(
            selectedFiles = selectedFiles,
            onDismiss = { showBatchRename = false },
            onConfirm = { prefix, suffix, find, replace, includeIndex ->
                scope.launch {
                    engine.batchRename(
                        files = selectedFiles.map { File(it.path) },
                        prefix = prefix,
                        suffix = suffix,
                        replaceFind = find,
                        replaceWith = replace,
                        includeIndex = includeIndex
                    )
                    selectedFiles.clear()
                    refreshFiles()
                }
            }
        )
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("CREATE FOLDER", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        focusedTextColor = NothingWhite,
                        unfocusedTextColor = NothingWhite
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderName.isNotEmpty()) {
                            scope.launch {
                                engine.createFolder(File(currentTab.path), folderName)
                                showCreateFolderDialog = false
                                refreshFiles()
                            }
                        }
                    }
                ) {
                    Text("CREATE", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // Create File Dialog
    if (showCreateFileDialog) {
        var fileName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = { Text("CREATE FILE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
            text = {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name (e.g. note.txt, script.sh)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        focusedTextColor = NothingWhite,
                        unfocusedTextColor = NothingWhite
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fileName.isNotEmpty()) {
                            scope.launch {
                                engine.createFile(File(currentTab.path), fileName)
                                showCreateFileDialog = false
                                refreshFiles()
                            }
                        }
                    }
                ) {
                    Text("CREATE", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) {
                    Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("DELETE FILES?", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed) },
            text = {
                Text("Are you sure you want to permanently delete ${selectedFiles.size} items? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            engine.deleteFiles(selectedFiles.map { File(it.path) })
                            selectedFiles.clear()
                            showDeleteConfirmDialog = false
                            refreshFiles()
                        }
                    }
                ) {
                    Text("DELETE", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileExplorerRowItem(
    file: FileItem,
    viewMode: ViewMode = ViewMode.DETAILED,
    zoomScale: Float = 1.0f,
    isPinned: Boolean,
    tag: String?,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit,
    onMenuClick: () -> Unit
) {
    val isCompact = viewMode == ViewMode.COMPACT
    val isMinimal = viewMode == ViewMode.MINIMAL
    val isWrapped = viewMode == ViewMode.WRAPPED
    val context = LocalContext.current

    val vertPadding = ((if (isCompact) 3f else if (isMinimal) 4f else 6f) * zoomScale).coerceIn(1f, 32f).dp
    val baseIconSize = if (isCompact) 26.dp else if (isMinimal) 24.dp else 36.dp
    val scaledIconSize = (baseIconSize.value * zoomScale).coerceIn(12f, 160f).dp
    val titleFontSize = ((if (isCompact) 13f else 14f) * zoomScale).coerceIn(9f, 28f).sp
    val subtitleFontSize = (10f * zoomScale).coerceIn(7f, 20f).sp
    val countFontSize = ((if (isCompact) 10f else 11f) * zoomScale).coerceIn(7f, 22f).sp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NothingSurfaceElevated else NothingBlack),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left portion: Dedicated touch area to SELECT / toggle selection of this file
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onToggleSelect)
                .padding(
                    start = ((if (isCompact) 6f else 8f) * zoomScale.coerceIn(0.6f, 2.0f)).dp,
                    end = ((if (isCompact) 4f else 6f) * zoomScale.coerceIn(0.6f, 2.0f)).dp,
                    top = vertPadding,
                    bottom = vertPadding
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.TopStart) {
                FileThumbnailView(
                    file = file,
                    modifier = Modifier.size(scaledIconSize),
                    iconScale = zoomScale * (if (isCompact || isMinimal) 0.8f else 1.0f),
                    isPinned = isPinned
                )
                if (isSelectionMode || isSelected) {
                    Box(
                        modifier = Modifier
                            .size((16f * zoomScale).coerceIn(10f, 24f).dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(if (isSelected) LedOnRed else NothingBlack.copy(alpha = 0.8f))
                            .border(1.dp, if (isSelected) LedOnRed else NothingBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = NothingWhite,
                                modifier = Modifier.size((10f * zoomScale).coerceIn(6f, 16f).dp)
                            )
                        }
                    }
                }
            }
        }

        // Right portion: Clicking anywhere opens the file/folder/archive
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onOpen,
                    onLongClick = onToggleSelect
                )
                .padding(
                    start = 4.dp,
                    end = ((if (isCompact) 6f else 8f) * zoomScale.coerceIn(0.6f, 2.0f)).dp,
                    top = vertPadding,
                    bottom = vertPadding
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tag if assigned
            if (tag != null && !isMinimal) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NothingBorder)
                        .padding(horizontal = (5f * zoomScale).coerceIn(3f, 12f).dp, vertical = (2f * zoomScale).coerceIn(1f, 6f).dp)
                ) {
                    Text(
                        text = tag.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = (9f * zoomScale).coerceIn(6f, 18f).sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Name and Subtitle
            Column(modifier = Modifier.weight(1f)) {
                val isPackageFolder = file.isDirectory && file.name.contains(".") && (
                    file.path.contains("/Android/data", ignoreCase = true) ||
                    file.path.contains("/Android/obb", ignoreCase = true) ||
                    file.path.contains("/Android/media", ignoreCase = true)
                )
                val appLabel = if (isPackageFolder) ThumbnailEngine.getAppLabel(context, file.name) else null

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = file.name,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = titleFontSize,
                        color = if (isSelected) NothingRed else NothingWhite,
                        maxLines = if (isWrapped) 4 else 1,
                        overflow = if (isWrapped) TextOverflow.Clip else TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!appLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(NothingRed.copy(alpha = 0.15f))
                                .border(0.6.dp, NothingRed.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = appLabel ?: "",
                                fontFamily = FontFamily.Monospace,
                                fontSize = (9f * zoomScale).coerceIn(7f, 13f).sp,
                                fontWeight = FontWeight.Bold,
                                color = NothingWhite
                            )
                        }
                    }
                }
                if (viewMode == ViewMode.DETAILED) {
                    Text(
                        text = "${file.formattedDate}   ${file.permissions}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = subtitleFontSize,
                        color = NothingTextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Right side: Item count in parentheses `(58)` for folders, or size for files
            if (!isMinimal) {
                Text(
                    text = if (file.isDirectory) "(${file.childCount})" else file.formattedSize,
                    fontFamily = FontFamily.Monospace,
                    fontSize = countFontSize,
                    color = NothingTextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileExplorerGridItem(
    file: FileItem,
    zoomScale: Float = 1.0f,
    isPinned: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val cardHeight = (115f * zoomScale).coerceIn(50f, 380f).dp
    val iconSize = (42f * zoomScale).coerceIn(18f, 180f).dp
    val titleFontSize = (11f * zoomScale).coerceIn(7.5f, 24f).sp
    val subtitleFontSize = (9f * zoomScale).coerceIn(6f, 18f).sp
    val context = LocalContext.current

    NothingCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape((8f * zoomScale).coerceIn(4f, 16f).dp),
        backgroundColor = if (isSelected) NothingSurfaceElevated else NothingSurface,
        borderColor = if (isSelected) NothingRed else NothingBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onClick = onOpen, onLongClick = onToggleSelect)
                .padding((8f * zoomScale).coerceIn(3f, 16f).dp)
        ) {
            // Left/Top-left dedicated selection toggle target (Always shows checkmark when selected)
            Box(
                modifier = Modifier
                    .size((24f * zoomScale).coerceIn(16f, 32f).dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(if (isSelected) LedOnRed else if (isSelectionMode) NothingBlack.copy(alpha = 0.7f) else Color.Transparent)
                    .border(1.dp, if (isSelected) LedOnRed else if (isSelectionMode) NothingBorder else Color.Transparent, CircleShape)
                    .clickable(onClick = onToggleSelect),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size((12f * zoomScale).coerceIn(8f, 18f).dp)
                    )
                }
            }

            if (isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = NothingRed,
                    modifier = Modifier.size((12f * zoomScale).coerceIn(8f, 22f).dp).align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FileThumbnailView(
                    file = file,
                    modifier = Modifier.size(iconSize),
                    iconScale = zoomScale * 1.3f,
                    isPinned = false
                )

                Spacer(modifier = Modifier.height((6f * zoomScale).coerceIn(2f, 16f).dp))

                val isPackageFolder = file.isDirectory && file.name.contains(".") && (
                    file.path.contains("/Android/data", ignoreCase = true) ||
                    file.path.contains("/Android/obb", ignoreCase = true) ||
                    file.path.contains("/Android/media", ignoreCase = true)
                )
                val appLabel = if (isPackageFolder) ThumbnailEngine.getAppLabel(context, file.name) else null

                Text(
                    text = appLabel ?: file.name,
                    fontFamily = FontFamily.Default,
                    fontWeight = if (appLabel != null) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = titleFontSize,
                    color = if (isSelected) NothingRed else NothingWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (appLabel != null) file.name else (if (file.isDirectory) "(${file.childCount})" else file.formattedSize),
                    fontFamily = FontFamily.Monospace,
                    fontSize = subtitleFontSize,
                    color = NothingTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileExplorerGalleryItem(
    file: FileItem,
    zoomScale: Float = 1.0f,
    isPinned: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val cardHeight = (145f * zoomScale).coerceIn(60f, 480f).dp
    val titleFontSize = (11f * zoomScale).coerceIn(7.5f, 24f).sp
    val subtitleFontSize = (9f * zoomScale).coerceIn(6f, 18f).sp

    NothingCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape((10f * zoomScale).coerceIn(4f, 18f).dp),
        backgroundColor = NothingBlack,
        borderColor = if (isSelected) NothingRed else NothingBorder
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onClick = onOpen, onLongClick = onToggleSelect)
        ) {
            FileThumbnailView(
                file = file,
                modifier = Modifier.fillMaxSize(),
                iconScale = zoomScale * 1.8f,
                isPinned = false
            )

            // Top-left selection button (Always shows checkmark when selected)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding((6f * zoomScale).coerceIn(3f, 12f).dp)
                    .size((24f * zoomScale).coerceIn(16f, 32f).dp)
                    .clip(CircleShape)
                    .background(if (isSelected) LedOnRed else if (isSelectionMode) NothingBlack.copy(alpha = 0.7f) else Color.Transparent)
                    .border(1.dp, if (isSelected) LedOnRed else if (isSelectionMode) NothingBorder else Color.Transparent, CircleShape)
                    .clickable(onClick = onToggleSelect),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size((12f * zoomScale).coerceIn(8f, 18f).dp)
                    )
                }
            }

            // Bottom Gradient Overlay with file title and size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(horizontal = (8f * zoomScale).coerceIn(4f, 16f).dp, vertical = (6f * zoomScale).coerceIn(3f, 12f).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = file.name,
                        fontFamily = FontFamily.Default,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Medium,
                        color = NothingWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (file.isDirectory) "(${file.childCount})" else file.formattedSize,
                        fontFamily = FontFamily.Monospace,
                        fontSize = subtitleFontSize,
                        color = NothingTextSecondary
                    )
                }
            }

            if (isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = NothingRed,
                    modifier = Modifier.size((14f * zoomScale).coerceIn(8f, 22f).dp).align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileExplorerCompactRow(
    file: FileItem,
    zoomScale: Float = 1.0f,
    isPinned: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onOpen: () -> Unit,
    onToggleSelect: () -> Unit
) {
    val rowHeight = (44f * zoomScale).coerceIn(24f, 140f).dp
    val iconWidth = (40f * zoomScale).coerceIn(20f, 100f).dp
    val iconSize = (22f * zoomScale).coerceIn(12f, 80f).dp
    val titleFontSize = (12f * zoomScale).coerceIn(8f, 24f).sp
    val countFontSize = (10f * zoomScale).coerceIn(7f, 20f).sp

    NothingCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight),
        shape = RoundedCornerShape((6f * zoomScale).coerceIn(3f, 12f).dp),
        backgroundColor = if (isSelected) NothingSurfaceElevated else NothingSurface,
        borderColor = if (isSelected) NothingRed else NothingBorder
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left touch area dedicated to selection (Thumbnail always visible)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(iconWidth)
                    .clickable(onClick = onToggleSelect),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.TopStart) {
                    FileThumbnailView(
                        file = file,
                        modifier = Modifier.size(iconSize),
                        iconScale = zoomScale * 0.8f,
                        isPinned = isPinned
                    )
                    if (isSelectionMode || isSelected) {
                        Box(
                            modifier = Modifier
                                .size((14f * zoomScale).coerceIn(9f, 20f).dp)
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(if (isSelected) LedOnRed else NothingBlack.copy(alpha = 0.8f))
                                .border(1.dp, if (isSelected) LedOnRed else NothingBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NothingWhite,
                                    modifier = Modifier.size((9f * zoomScale).coerceIn(6f, 14f).dp)
                                )
                            }
                        }
                    }
                }
            }

            // Right open area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .combinedClickable(onClick = onOpen, onLongClick = onToggleSelect)
                    .padding(end = (8f * zoomScale).coerceIn(4f, 16f).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = file.name,
                    fontFamily = FontFamily.Default,
                    fontSize = titleFontSize,
                    color = if (isSelected) NothingRed else NothingWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (file.isDirectory) "(${file.childCount})" else file.formattedSize,
                    fontFamily = FontFamily.Monospace,
                    fontSize = countFontSize,
                    color = NothingTextSecondary
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

private fun getLocalIpAddress(context: Context): String {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val ip = wifiManager?.connectionInfo?.ipAddress ?: 0
    if (ip != 0) {
        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }
    return "127.0.0.1"
}

private fun shareFiles(context: Context, files: List<FileItem>) {
    if (files.isEmpty()) return
    try {
        if (files.size == 1) {
            val file = File(files.first().path)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val ext = file.extension.lowercase()
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
        } else {
            val uris = ArrayList<Uri>()
            for (f in files) {
                uris.add(FileProvider.getUriForFile(context, "${context.packageName}.provider", File(f.path)))
            }
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${files.size} files"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing files: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun openWithExternalApp(fileItem: FileItem, context: Context) {
    try {
        val file = File(fileItem.path)
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val ext = file.extension.lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.2f %s", value, units[digitGroups])
}

@Composable
fun ClipboardTasksDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    tasks: List<ClipboardTask>,
    currentFolderName: String,
    onExecuteTask: (ClipboardTask) -> Unit,
    onExecuteAll: () -> Unit,
    onClear: () -> Unit,
    onRemoveTask: (ClipboardTask) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .widthIn(min = 280.dp, max = 340.dp)
            .background(NothingBlack)
            .border(1.dp, NothingBorder, RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        // Header showing count & target destination
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TASKS (${tasks.size})",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NothingTextSecondary
            )
            Text(
                text = "TARGET: $currentFolderName",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = NothingRed,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HorizontalDivider(color = NothingBorder, modifier = Modifier.padding(vertical = 4.dp))

        // Task list in reverse order (Matching MiXplorer Screenshot 1: #3 on top, #1 on bottom)
        tasks.asReversed().forEachIndexed { index, task ->
            val taskNum = tasks.size - index
            val actionColor = when (task.action) {
                ClipboardAction.EXTRACT, ClipboardAction.ARCHIVE -> NothingRed
                ClipboardAction.MOVE -> NothingWhite
                ClipboardAction.COPY -> NothingWhite
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        onDismissRequest()
                        onExecuteTask(task)
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${task.action.name}   ${if (task.files.size == 1) "One file" else "${task.files.size} files"}",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = actionColor
                        )
                        Text(
                            text = "#$taskNum NOT STARTED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NothingTextSecondary
                        )
                        val descriptionText = when (task.action) {
                            ClipboardAction.ARCHIVE -> {
                                val arcName = task.archiveConfig?.archiveName ?: "Archive.zip"
                                "$arcName (${task.files.size} items)"
                            }
                            else -> {
                                if (task.files.size == 1) task.files.first().name else "${task.files.first().name} (+${task.files.size - 1})"
                            }
                        }
                        Text(
                            text = descriptionText,
                            fontFamily = FontFamily.Default,
                            fontSize = 11.sp,
                            color = NothingWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { onRemoveTask(task) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel task",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = NothingBorder, modifier = Modifier.padding(vertical = 4.dp))

        // Bottom row: CLEAR on left, EXECUTE ALL HERE on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onClear
            ) {
                Text(
                    text = "CLEAR",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NothingTextSecondary
                )
            }

            TextButton(
                onClick = {
                    onDismissRequest()
                    onExecuteAll()
                }
            ) {
                Text(
                    text = "EXECUTE ALL HERE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NothingRed
                )
            }
        }
    }
}

