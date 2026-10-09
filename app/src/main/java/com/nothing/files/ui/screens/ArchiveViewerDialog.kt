package com.nothing.files.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nothing.files.engine.ArchiveEngine
import com.nothing.files.engine.ArchiveEntry
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ArchiveViewerDialog(
    archiveFile: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { ArchiveEngine() }

    var entries by remember { mutableStateOf<List<ArchiveEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isExtracting by remember { mutableStateOf(false) }
    var extractProgress by remember { mutableStateOf("") }

    LaunchedEffect(archiveFile.path) {
        entries = engine.listZipEntries(File(archiveFile.path))
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        NothingCard(
            shape = RoundedCornerShape(16.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ARCHIVE VIEWER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NothingRed
                        )
                        Text(
                            text = archiveFile.name,
                            maxLines = 1,
                            fontSize = 12.sp,
                            color = NothingTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("READING ARCHIVE...", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingWhite)
                    }
                } else {
                    Text(
                        text = "${entries.size} ENTRIES FOUND:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    NothingCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = NothingSurfaceElevated,
                        borderColor = NothingBorder
                    ) {
                        LazyColumn(modifier = Modifier.padding(6.dp)) {
                            items(entries) { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.Default.Archive,
                                            contentDescription = null,
                                            tint = if (entry.isDirectory) NothingWhite else NothingRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = entry.name,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            color = NothingWhite
                                        )
                                    }

                                    if (!entry.isDirectory && entry.uncompressedSize > 0) {
                                        Text(
                                            text = formatSize(entry.uncompressedSize),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = NothingTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isExtracting) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "EXTRACTING: $extractProgress",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingRed
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CLOSE", fontFamily = FontFamily.Monospace, color = NothingTextSecondary)
                    }

                    TextButton(
                        enabled = !isExtracting && !isLoading,
                        onClick = {
                            isExtracting = true
                            scope.launch {
                                val targetDir = File(File(archiveFile.path).parentFile, archiveFile.name.substringBeforeLast('.'))
                                val success = engine.extractZip(File(archiveFile.path), targetDir) { curr, total, file ->
                                    extractProgress = "$curr / $total"
                                }
                                isExtracting = false
                                if (success) {
                                    Toast.makeText(context, "Extracted to ${targetDir.name}", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Extraction failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("EXTRACT ALL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingRed)
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.1f %s", value, units[digitGroups])
}
