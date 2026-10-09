package com.nothing.files.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.ArchiveEngine
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ExtractTargetMode {
    SUBFOLDER,
    CURRENT_DIR,
    CUSTOM_PATH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractArchiveDialog(
    targetArchives: List<FileItem>,
    currentDirectory: String,
    onDismiss: () -> Unit,
    onExtracted: () -> Unit
) {
    if (targetArchives.isEmpty()) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val archiveEngine = remember { ArchiveEngine() }

    val isSingle = targetArchives.size == 1
    val singleItem = targetArchives.first()
    val defaultSubfolderName = remember {
        if (isSingle) {
            val f = File(singleItem.path)
            f.nameWithoutExtension.ifBlank { "Extracted" }
        } else {
            "Extracted_Archives"
        }
    }

    var selectedMode by remember { mutableStateOf(ExtractTargetMode.SUBFOLDER) }
    var subfolderName by remember { mutableStateOf(defaultSubfolderName) }
    var customDestinationPath by remember { mutableStateOf(currentDirectory) }
    var deleteArchiveAfterExtract by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var currentFileProgress by remember { mutableStateOf("") }
    var progressFraction by remember { mutableFloatStateOf(0f) }

    fun executeExtraction() {
        isProcessing = true
        scope.launch {
            var allSucceeded = true
            var extractedCount = 0

            withContext(Dispatchers.IO) {
                targetArchives.forEachIndexed { index, archiveItem ->
                    val archiveFile = File(archiveItem.path)
                    val baseTarget = when (selectedMode) {
                        ExtractTargetMode.SUBFOLDER -> {
                            val folderName = if (isSingle) subfolderName.trim().ifBlank { defaultSubfolderName }
                            else archiveFile.nameWithoutExtension.ifBlank { "Archive_${index + 1}" }
                            File(currentDirectory, folderName)
                        }
                        ExtractTargetMode.CURRENT_DIR -> File(currentDirectory)
                        ExtractTargetMode.CUSTOM_PATH -> {
                            val cleanPath = customDestinationPath.trim().ifBlank { currentDirectory }
                            File(cleanPath)
                        }
                    }

                    progressStatus = "Extracting (${index + 1}/${targetArchives.size}): ${archiveFile.name}"

                    val ok = archiveEngine.extractArchive(
                        archiveFile = archiveFile,
                        targetDir = baseTarget,
                        onProgress = { curr, total, file ->
                            if (total > 0) {
                                progressFraction = curr.toFloat() / total.toFloat()
                                progressStatus = "Extracted $curr / $total files"
                            }
                            currentFileProgress = file
                        }
                    )

                    if (ok) {
                        extractedCount++
                        if (deleteArchiveAfterExtract) {
                            try {
                                archiveFile.delete()
                            } catch (_: Exception) {}
                        }
                    } else {
                        allSucceeded = false
                    }
                }
            }

            isProcessing = false
            if (allSucceeded && extractedCount > 0) {
                val targetDesc = when (selectedMode) {
                    ExtractTargetMode.SUBFOLDER -> subfolderName
                    ExtractTargetMode.CURRENT_DIR -> "current folder"
                    ExtractTargetMode.CUSTOM_PATH -> customDestinationPath
                }
                Toast.makeText(context, "Extracted $extractedCount archive(s) to $targetDesc", Toast.LENGTH_LONG).show()
                onExtracted()
                onDismiss()
            } else if (extractedCount > 0) {
                Toast.makeText(context, "Partially extracted ($extractedCount / ${targetArchives.size})", Toast.LENGTH_LONG).show()
                onExtracted()
                onDismiss()
            } else {
                Toast.makeText(context, "Extraction failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BasicAlertDialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = NothingRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSingle) "EXTRACT ARCHIVE" else "EXTRACT ${targetArchives.size} ARCHIVES",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NothingWhite
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!isProcessing) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Archive info banner
                NothingCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = NothingSurfaceElevated,
                    borderColor = NothingBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = NothingWhite,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSingle) singleItem.name else "${targetArchives.size} archives selected",
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = NothingWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val totalBytes = targetArchives.sumOf { it.size }
                            Text(
                                text = formatFileSize(totalBytes),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = NothingTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isProcessing) {
                    Text(
                        text = "DESTINATION",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NothingRed
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 1: Extract to Subfolder (Recommended)
                    DestinationOptionRow(
                        selected = selectedMode == ExtractTargetMode.SUBFOLDER,
                        title = if (isSingle) "Extract to subfolder" else "Extract each to its own folder",
                        subtitle = if (isSingle) "/$subfolderName/" else "Keeps extracted files organized in folders",
                        onClick = { selectedMode = ExtractTargetMode.SUBFOLDER }
                    )

                    if (selectedMode == ExtractTargetMode.SUBFOLDER && isSingle) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = subfolderName,
                            onValueChange = { subfolderName = it },
                            label = { Text("Folder Name", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite,
                                cursorColor = NothingRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 2: Extract directly here
                    DestinationOptionRow(
                        selected = selectedMode == ExtractTargetMode.CURRENT_DIR,
                        title = "Extract here",
                        subtitle = "Directly into current directory",
                        onClick = { selectedMode = ExtractTargetMode.CURRENT_DIR }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option 3: Custom path
                    DestinationOptionRow(
                        selected = selectedMode == ExtractTargetMode.CUSTOM_PATH,
                        title = "Custom destination path",
                        subtitle = customDestinationPath,
                        onClick = { selectedMode = ExtractTargetMode.CUSTOM_PATH }
                    )

                    if (selectedMode == ExtractTargetMode.CUSTOM_PATH) {
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customDestinationPath,
                            onValueChange = { customDestinationPath = it },
                            label = { Text("Destination Directory", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite,
                                cursorColor = NothingRed
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Delete after extract checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deleteArchiveAfterExtract = !deleteArchiveAfterExtract }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = deleteArchiveAfterExtract,
                            onCheckedChange = { deleteArchiveAfterExtract = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NothingRed,
                                uncheckedColor = NothingBorder,
                                checkmarkColor = NothingWhite
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Delete archive after extraction",
                            fontFamily = FontFamily.Default,
                            fontSize = 12.sp,
                            color = NothingWhite
                        )
                    }
                } else {
                    // Extracting progress state
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = NothingRed,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = progressStatus.ifBlank { "EXTRACTING ARCHIVE..." },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NothingRed
                        )
                        if (currentFileProgress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentFileProgress,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = NothingTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (progressFraction > 0f) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = NothingRed,
                                trackColor = NothingBorder
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "CANCEL",
                            fontFamily = FontFamily.Monospace,
                            color = NothingTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = { executeExtraction() },
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "EXTRACT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isProcessing) NothingTextSecondary else NothingRed,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationOptionRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    NothingCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = if (selected) NothingSurfaceElevated else NothingBlack,
        borderColor = if (selected) NothingRed else NothingBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio circle
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (selected) NothingRed else NothingBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NothingRed)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Default,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = NothingWhite
                )
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NothingTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.1f %s", value, units[digitGroups])
}
