package com.nothing.files.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nothing.files.engine.AppHandlerInfo
import com.nothing.files.engine.FileIdentifierEngine
import com.nothing.files.engine.ThumbnailEngine
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.FileThumbnailView
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingAccentGreen
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OpenFileOptionsDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    onOpenTextEditor: (FileItem) -> Unit,
    onBrowseArchive: ((FileItem) -> Unit)? = null,
    onShare: (FileItem) -> Unit
) {
    val context = LocalContext.current
    var uri by remember { mutableStateOf<Uri?>(null) }
    var mimeType by remember { mutableStateOf(fileItem.mimeType) }
    var relevantApps by remember { mutableStateOf<List<AppHandlerInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(true) }

    LaunchedEffect(fileItem.path) {
        isLoadingApps = true
        withContext(Dispatchers.IO) {
            val resolvedUri = FileIdentifierEngine.resolveFileUri(context, fileItem)
            val resolvedMime = FileIdentifierEngine.getMimeType(fileItem.extension)
            val apps = FileIdentifierEngine.getRelevantApps(context, resolvedUri, resolvedMime)
            uri = resolvedUri
            mimeType = resolvedMime
            relevantApps = apps
            isLoadingApps = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = NothingBlack,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Top Header: Tag + Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(NothingRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FILE IDENTIFIER & OPEN WITH",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NothingRed,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // File Identifier Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NothingSurfaceElevated)
                        .border(1.dp, NothingBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FileThumbnailView(
                            file = fileItem,
                            modifier = Modifier.size(44.dp),
                            iconScale = 1.2f
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileItem.name,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NothingWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(NothingRed.copy(alpha = 0.15f))
                                        .border(0.6.dp, NothingRed.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = fileItem.dotMatrixTypeTag,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NothingRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${fileItem.fileTypeDescription} • ${fileItem.formattedSize}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = NothingTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: INSTALLED COMPATIBLE APPS
                Text(
                    text = "RELEVANT APPS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = NothingTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoadingApps) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NothingRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (relevantApps.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(relevantApps, key = { it.packageName + it.activityName }) { app ->
                            AppHandlerCard(
                                app = app,
                                onClick = {
                                    val targetUri = uri ?: FileIdentifierEngine.resolveFileUri(context, fileItem)
                                    FileIdentifierEngine.openWithApp(
                                        context = context,
                                        uri = targetUri,
                                        mimeType = mimeType,
                                        packageName = app.packageName,
                                        activityName = app.activityName
                                    )
                                    onDismiss()
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingSurface)
                            .border(0.6.dp, NothingBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No dedicated external app registered for this file",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = NothingTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section: "OPEN AS" ACTIONS
                Text(
                    text = "OPEN AS / ACTIONS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = NothingTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 1: Built-in Code / Text Editor
                    QuickActionRow(
                        icon = Icons.Default.Code,
                        iconTint = NothingAccentGreen,
                        title = "Built-in Text & Code Editor",
                        subtitle = "View and edit source code, scripts, configs, logs",
                        onClick = {
                            onOpenTextEditor(fileItem)
                            onDismiss()
                        }
                    )

                    // Option 2: Archive Explorer (if archive or APK)
                    if ((fileItem.isArchive || fileItem.isApk) && onBrowseArchive != null) {
                        QuickActionRow(
                            icon = Icons.Default.FolderOpen,
                            iconTint = NothingWhite,
                            title = "Browse Inside Archive",
                            subtitle = "Explore classes.dex, resources, internal directory tree",
                            onClick = {
                                onBrowseArchive(fileItem)
                                onDismiss()
                            }
                        )
                    }

                    // Option 3: System Intent Chooser
                    QuickActionRow(
                        icon = Icons.Default.OpenInNew,
                        iconTint = NothingRed,
                        title = "Android System Chooser...",
                        subtitle = "Show complete system app picker dialog",
                        onClick = {
                            val targetUri = uri ?: FileIdentifierEngine.resolveFileUri(context, fileItem)
                            FileIdentifierEngine.openSystemChooser(
                                context = context,
                                uri = targetUri,
                                mimeType = mimeType,
                                title = "Open ${fileItem.name}"
                            )
                            onDismiss()
                        }
                    )

                    // Option 4: Share
                    QuickActionRow(
                        icon = Icons.Default.Share,
                        iconTint = NothingTextSecondary,
                        title = "Share File",
                        subtitle = "Send via Quick Share, Bluetooth, messaging apps",
                        onClick = {
                            onShare(fileItem)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHandlerCard(
    app: AppHandlerInfo,
    onClick: () -> Unit
) {
    val iconBitmap = remember(app.icon) {
        app.icon?.let { ThumbnailEngine.drawableToBitmap(it, 96, 96) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NothingSurfaceElevated)
            .border(1.dp, NothingBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NothingSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                fontFamily = FontFamily.Default,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = NothingWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NothingSurface)
            .border(0.6.dp, NothingBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NothingWhite
                )
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.Default,
                    fontSize = 9.sp,
                    color = NothingTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
