package com.nothing.files.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileInfoDialog(
    targetFiles: List<FileItem>,
    onDismiss: () -> Unit,
    onOpenChecksum: (FileItem) -> Unit,
    onOpenPermissions: (FileItem) -> Unit
) {
    if (targetFiles.isEmpty()) return

    val context = LocalContext.current
    val primary = targetFiles.first()
    val fileObj = remember { File(primary.path) }

    var extraMetadata by remember { mutableStateOf<String?>(null) }
    var mimeType by remember { mutableStateOf(getMimeType(fileObj)) }
    var relativeTime by remember { mutableStateOf("") }

    val totalBytes = remember {
        targetFiles.sumOf { it.size }
    }

    LaunchedEffect(primary.path) {
        withContext(Dispatchers.IO) {
            // Extract media metadata if video or audio
            if (primary.isVideo || primary.isAudio) {
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(primary.path)
                    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)

                    val sec = (durationMs / 1000) % 60
                    val min = (durationMs / (1000 * 60)) % 60
                    val hr = (durationMs / (1000 * 60 * 60))
                    val durStr = if (hr > 0) String.format("%02d:%02d:%02d", hr, min, sec) else String.format("%02d:%02d", min, sec)

                    extraMetadata = if (width != null && height != null) {
                        "Device: /storage/emulated\n$durStr | $width × $height"
                    } else {
                        "Device: /storage/emulated\nDuration: $durStr"
                    }
                    retriever.release()
                } catch (_: Exception) {}
            } else if (primary.isImage) {
                try {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(primary.path, options)
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        extraMetadata = "Resolution: ${options.outWidth} × ${options.outHeight}"
                    }
                } catch (_: Exception) {}
            }

            // Relative time calculation
            val diffMs = System.currentTimeMillis() - primary.lastModified
            val days = diffMs / (1000 * 60 * 60 * 24)
            relativeTime = when {
                days == 0L -> "Today"
                days == 1L -> "One day ago"
                days < 30L -> "$days days ago"
                else -> "${days / 30} months ago"
            }
        }
    }

    val numFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val formattedModified = remember {
        val sdf = SimpleDateFormat("dd MMM yyyy h:mm a", Locale.US)
        sdf.format(Date(primary.lastModified))
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp)
                    .padding(20.dp)
            ) {
                // Header (Thumbnail + Name + Close Button) - Matching Screenshot 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview Icon / Thumbnail
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingBlack)
                            .border(1.dp, NothingBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (primary.isImage || primary.isVideo) {
                            AsyncImage(
                                model = primary.path,
                                contentDescription = null,
                                modifier = Modifier.size(46.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val icon = when {
                                primary.isDirectory -> Icons.Default.Folder
                                primary.isAudio -> Icons.Default.MusicNote
                                primary.isVideo -> Icons.Default.Movie
                                primary.isImage -> Icons.Default.Image
                                else -> Icons.Default.Description
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = NothingRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (targetFiles.size > 1) "${targetFiles.size} items selected" else primary.name,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NothingWhite,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = NothingBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Info Details (Screenshot 2)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    InfoPropertyRow(
                        label = "Path:",
                        value = primary.path,
                        isClickable = true,
                        onClick = { copyToClipboard(primary.path, "Path") }
                    )

                    InfoPropertyRow(
                        label = "Content URI:",
                        value = "content://com.nothing.files.provider${primary.path}",
                        isClickable = true,
                        onClick = { copyToClipboard("content://com.nothing.files.provider${primary.path}", "Content URI") }
                    )

                    // Size line
                    val primaryBytesStr = numFormat.format(primary.size)
                    val primarySizeStr = primary.formattedSize
                    InfoPropertyRow(
                        label = "Size:",
                        value = "$primarySizeStr ($primaryBytesStr B)"
                    )

                    if (targetFiles.size > 1) {
                        val totalBytesStr = numFormat.format(totalBytes)
                        InfoPropertyRow(
                            label = "Total size:",
                            value = "${formatBytes(totalBytes)} ($totalBytesStr B)\n${targetFiles.size} files"
                        )
                    }

                    // Disk Used / Effective
                    val usedBytes = (primary.size / 4096 + 1) * 4096
                    val usedStr = formatBytes(usedBytes)
                    val usedBytesFormatted = numFormat.format(usedBytes)
                    InfoPropertyRow(
                        label = "Used:",
                        value = "$usedStr ($usedBytesFormatted B)\nEffective: $usedStr"
                    )

                    if (targetFiles.size > 1) {
                        val totalUsed = (totalBytes / 4096 + targetFiles.size) * 4096
                        InfoPropertyRow(
                            label = "Total used:",
                            value = "${formatBytes(totalUsed)} (${numFormat.format(totalUsed)} B)"
                        )
                    }

                    // Dates
                    InfoPropertyRow(
                        label = "Modified:",
                        value = "$formattedModified\n$relativeTime"
                    )

                    InfoPropertyRow(
                        label = "Changed:",
                        value = formattedModified
                    )

                    InfoPropertyRow(
                        label = "Type:",
                        value = if (primary.isDirectory) "Directory" else mimeType
                    )

                    InfoPropertyRow(
                        label = "Hidden:",
                        value = if (primary.isHidden) "Yes" else "No"
                    )

                    // Permissions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Permissions:",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = NothingTextSecondary,
                            modifier = Modifier.width(100.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onOpenPermissions(primary) }
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "${primary.permissions} (Edit)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = NothingRed
                            )
                        }
                    }

                    // Metadata (Duration, Resolution)
                    if (extraMetadata != null) {
                        InfoPropertyRow(
                            label = "Metadata:",
                            value = extraMetadata ?: ""
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Comment",
                        fontFamily = FontFamily.Default,
                        fontSize = 13.sp,
                        color = NothingTextSecondary
                    )
                    HorizontalDivider(
                        color = NothingBorder,
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action: CHECKSUM Button (Screenshot 2)
                Button(
                    onClick = {
                        onDismiss()
                        onOpenChecksum(primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NothingSurfaceElevated,
                        contentColor = NothingWhite
                    )
                ) {
                    Text(
                        text = "CHECKSUM",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoPropertyRow(
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = NothingTextSecondary,
            modifier = Modifier.width(100.dp)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (isClickable && onClick != null) {
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(onClick = onClick)
                            .padding(2.dp)
                    } else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = NothingWhite,
                modifier = Modifier.weight(1f)
            )

            if (isClickable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = NothingTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun getMimeType(file: File): String {
    val ext = file.extension.lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KiB", "MiB", "GiB", "TiB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format(Locale.US, "%.2f %s", value, units[digitGroups])
}
