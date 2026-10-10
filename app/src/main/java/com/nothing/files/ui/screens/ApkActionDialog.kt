package com.nothing.files.ui.screens

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InstallMobile
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nothing.files.engine.ApkPackageDetails
import com.nothing.files.engine.FileIdentifierEngine
import com.nothing.files.model.FileItem
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

@Composable
fun ApkActionDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit,
    onBrowseArchive: (FileItem) -> Unit,
    onOpenWithOptions: (FileItem) -> Unit,
    onShare: (FileItem) -> Unit
) {
    val context = LocalContext.current
    var details by remember(fileItem.path) { mutableStateOf<ApkPackageDetails?>(null) }
    var isLoading by remember(fileItem.path) { mutableStateOf(true) }

    LaunchedEffect(fileItem.path) {
        isLoading = true
        details = FileIdentifierEngine.getApkDetails(context, fileItem.path)
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = NothingBlack,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header with Dot Indicator and Close button
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
                                .background(NothingAccentGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANDROID PACKAGE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NothingAccentGreen,
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

                // App Info Box
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NothingAccentGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App Icon
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NothingSurfaceElevated)
                                .border(1.dp, NothingBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = details?.iconBitmap
                            if (icon != null) {
                                Image(
                                    bitmap = icon.asImageBitmap(),
                                    contentDescription = details?.appName ?: fileItem.name,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = "Android App",
                                    tint = NothingAccentGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // App Title & Package
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = details?.appName ?: fileItem.name.substringBeforeLast('.'),
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NothingWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = details?.packageName?.ifEmpty { fileItem.name } ?: fileItem.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = NothingTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spec Chips Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ApkMetaBadge(
                            label = "VERSION",
                            value = details?.versionName?.take(14) ?: "Unknown",
                            modifier = Modifier.weight(1f)
                        )
                        ApkMetaBadge(
                            label = "SIZE",
                            value = fileItem.formattedSize,
                            modifier = Modifier.weight(1f)
                        )
                        ApkMetaBadge(
                            label = "TARGET",
                            value = details?.let { "API ${it.targetSdkVersion}" } ?: "API",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action: INSTALL APPLICATION
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NothingAccentGreen)
                        .clickable {
                            FileIdentifierEngine.installApk(context, fileItem)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "Install",
                            tint = NothingBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INSTALL APP",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NothingBlack,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Action: BROWSE INSIDE APK ARCHIVE (Allows inspecting files only if user explicitly wants)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NothingSurfaceElevated)
                        .border(1.dp, NothingBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            onBrowseArchive(fileItem)
                            onDismiss()
                        }
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Browse",
                            tint = NothingWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BROWSE INSIDE ARCHIVE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = NothingWhite
                            )
                            Text(
                                text = "Inspect classes.dex, res, AndroidManifest",
                                fontFamily = FontFamily.Default,
                                fontSize = 10.sp,
                                color = NothingTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Open with... & Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingSurface)
                            .border(1.dp, NothingBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onOpenWithOptions(fileItem)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open with",
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OPEN WITH...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = NothingWhite
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingSurface)
                            .border(1.dp, NothingBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onShare(fileItem)
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHARE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = NothingWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApkMetaBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NothingSurfaceElevated)
            .border(0.6.dp, NothingBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = NothingTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NothingWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
