package com.nothing.files.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.ThumbnailEngine
import com.nothing.files.model.FileItem
import com.nothing.files.ui.theme.NothingAccentGreen
import com.nothing.files.ui.theme.NothingAccentPurple
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite

@Composable
fun FileThumbnailView(
    file: FileItem,
    modifier: Modifier = Modifier,
    iconScale: Float = 1.0f,
    isPinned: Boolean = false
) {
    val context = LocalContext.current
    val isPdf = file.extension == "pdf"
    val isPackageFolder = file.isDirectory && file.name.contains(".") && (
        file.path.contains("/Android/data", ignoreCase = true) ||
        file.path.contains("/Android/obb", ignoreCase = true) ||
        file.path.contains("/Android/media", ignoreCase = true) ||
        file.name.matches("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+$".toRegex())
    )
    val needsThumbnail = file.isApk || file.isVideo || isPdf || file.isImage

    var bitmap by remember(file.path, file.lastModified) {
        mutableStateOf<Bitmap?>(
            if (needsThumbnail) {
                val key = when {
                    file.isApk -> "apk_${file.path}_${file.lastModified}_128"
                    file.isVideo -> "video_${file.path}_${file.lastModified}_256"
                    isPdf -> "pdf_${file.path}_${file.lastModified}_280"
                    file.isImage -> "img_${file.path}_${file.lastModified}_256"
                    else -> null
                }
                key?.let { ThumbnailEngine.getFromCache(it) }
            } else null
        )
    }

    var appIconBitmap by remember(file.name) {
        mutableStateOf(if (isPackageFolder) ThumbnailEngine.getFromCache("app_${file.name}") else null)
    }

    LaunchedEffect(file.path, file.lastModified) {
        if (needsThumbnail && bitmap == null) {
            bitmap = ThumbnailEngine.loadFileThumbnail(context, file)
        }
    }

    LaunchedEffect(file.name) {
        if (isPackageFolder && appIconBitmap == null) {
            appIconBitmap = ThumbnailEngine.loadInstalledAppIcon(context, file.name)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape((6 * iconScale).coerceAtLeast(3f).dp))
            .background(NothingBlack),
        contentAlignment = Alignment.Center
    ) {
        when {
            file.isDirectory -> {
                val folderBoxSize = (28 * iconScale).coerceAtLeast(18f).dp
                Box(
                    modifier = Modifier.size(folderBoxSize),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Folder",
                        tint = NothingWhite,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (appIconBitmap != null) {
                        Image(
                            bitmap = appIconBitmap!!.asImageBitmap(),
                            contentDescription = file.name,
                            modifier = Modifier
                                .size((14 * iconScale).coerceIn(10f, 22f).dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 2.dp, y = 2.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(NothingBlack)
                                .border(1.dp, NothingBorder, RoundedCornerShape(3.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = if (isPdf || file.isApk) ContentScale.Fit else ContentScale.Crop
                )

                // Sleek Overlay Badges
                if (file.isVideo) {
                    Box(
                        modifier = Modifier
                            .size((16 * iconScale).coerceAtLeast(12f).dp)
                            .clip(CircleShape)
                            .background(NothingBlack.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NothingWhite,
                            modifier = Modifier.size((11 * iconScale).coerceAtLeast(8f).dp)
                        )
                    }
                } else if (isPdf) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(3.dp))
                            .background(NothingRed)
                            .padding(horizontal = 2.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "PDF",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite
                        )
                    }
                } else if (file.isApk) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(3.dp))
                            .background(NothingAccentGreen)
                            .padding(horizontal = 2.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "APK",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = NothingBlack
                        )
                    }
                }
            }

            file.isApk -> {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "APK",
                        tint = NothingAccentGreen,
                        modifier = Modifier.size((20 * iconScale).coerceAtLeast(14f).dp)
                    )
                }
            }

            file.isVideo -> {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video",
                    tint = NothingRed,
                    modifier = Modifier.size((20 * iconScale).coerceAtLeast(14f).dp)
                )
            }

            isPdf -> {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF",
                    tint = NothingRed,
                    modifier = Modifier.size((20 * iconScale).coerceAtLeast(14f).dp)
                )
            }

            file.isAudio -> {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Audio",
                    tint = NothingAccentGreen,
                    modifier = Modifier.size((18 * iconScale).coerceAtLeast(12f).dp)
                )
            }

            file.isArchive -> {
                Icon(
                    imageVector = Icons.Default.Archive,
                    contentDescription = "Archive",
                    tint = NothingRed,
                    modifier = Modifier.size((18 * iconScale).coerceAtLeast(12f).dp)
                )
            }

            else -> {
                Text(
                    text = file.dotMatrixTypeTag,
                    fontFamily = FontFamily.Monospace,
                    fontSize = (9 * iconScale).coerceIn(7f, 13f).sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingRed
                )
            }
        }

        if (isPinned) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pinned",
                tint = NothingRed,
                modifier = Modifier
                    .size((9 * iconScale).coerceAtLeast(7f).dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun AppIconThumbnailView(
    packageName: String,
    modifier: Modifier = Modifier,
    isSystemApp: Boolean = false,
    size: Dp = 36.dp
) {
    val context = LocalContext.current
    var bitmap by remember(packageName) {
        mutableStateOf(ThumbnailEngine.getFromCache("app_$packageName"))
    }

    LaunchedEffect(packageName) {
        if (bitmap == null) {
            bitmap = ThumbnailEngine.loadInstalledAppIcon(context, packageName)
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(NothingSurfaceElevated)
            .border(1.dp, NothingBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(2.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = if (isSystemApp) NothingTextSecondary else NothingAccentGreen,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
