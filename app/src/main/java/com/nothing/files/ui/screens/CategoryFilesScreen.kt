package com.nothing.files.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.nothing.files.engine.CategoryScannerEngine
import com.nothing.files.engine.FileIdentifierEngine
import com.nothing.files.model.FileItem
import com.nothing.files.model.NavItemType
import com.nothing.files.ui.components.FileItemRow
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import java.io.File

@Composable
fun CategoryFilesScreen(
    categoryType: NavItemType,
    categoryTitle: String,
    onBack: () -> Unit,
    onOpenTextEditor: (FileItem) -> Unit,
    onOpenHexViewer: (FileItem) -> Unit
) {
    val context = LocalContext.current
    val scannerEngine = remember { CategoryScannerEngine(context) }
    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var archiveFile by remember { mutableStateOf<FileItem?>(null) }
    var checksumFile by remember { mutableStateOf<FileItem?>(null) }
    var apkActionFile by remember { mutableStateOf<FileItem?>(null) }
    var openWithOptionsFile by remember { mutableStateOf<FileItem?>(null) }

    LaunchedEffect(categoryType) {
        isLoading = true
        files = scannerEngine.getFilesForCategory(categoryType)
        isLoading = false
    }

    val displayFiles = if (searchQuery.isNotEmpty()) {
        files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    } else {
        files
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // Top Bar
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NothingWhite
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = categoryTitle.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp,
                            color = NothingRed
                        )
                        Text(
                            text = "${files.size} files found",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NothingTextSecondary
                        )
                    }

                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NothingWhite
                        )
                    }
                }

                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter ${categoryTitle.lowercase()}...", fontSize = 12.sp) },
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

        // File List
        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
                }
            } else if (displayFiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO FILES IN THIS CATEGORY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NothingTextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(displayFiles, key = { it.path }) { file ->
                        FileItemRow(
                            file = file,
                            isSelected = false,
                            isSelectionMode = false,
                            onClick = {
                                when {
                                    file.isApk -> apkActionFile = file
                                    file.isArchive -> archiveFile = file
                                    file.isTextOrCode -> onOpenTextEditor(file)
                                    else -> openWithOptionsFile = file
                                }
                            },
                            onLongClick = {
                                checksumFile = file
                            },
                            onMenuClick = {
                                if (file.isApk) apkActionFile = file else openWithOptionsFile = file
                            }
                        )
                    }
                }
            }
        }
    }

    archiveFile?.let { arc ->
        ArchiveViewerDialog(archiveFile = arc, onDismiss = { archiveFile = null })
    }

    checksumFile?.let { chk ->
        ChecksumDialog(fileItem = chk, onDismiss = { checksumFile = null })
    }

    apkActionFile?.let { apk ->
        ApkActionDialog(
            fileItem = apk,
            onDismiss = { apkActionFile = null },
            onBrowseArchive = { archiveFile = it },
            onOpenWithOptions = { openWithOptionsFile = it },
            onShare = { openFileWithExternalApp(it, context) }
        )
    }

    openWithOptionsFile?.let { target ->
        OpenFileOptionsDialog(
            fileItem = target,
            onDismiss = { openWithOptionsFile = null },
            onOpenTextEditor = { onOpenTextEditor(it) },
            onBrowseArchive = if (target.isArchive || target.isApk) { { archiveFile = it } } else null,
            onShare = { openFileWithExternalApp(it, context) }
        )
    }
}

private fun openFileWithExternalApp(fileItem: FileItem, context: Context) {
    try {
        val uri = FileIdentifierEngine.resolveFileUri(context, fileItem)
        val mime = FileIdentifierEngine.getMimeType(fileItem.extension)
        FileIdentifierEngine.openSystemChooser(context, uri, mime, "Open ${fileItem.name}")
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
    }
}
