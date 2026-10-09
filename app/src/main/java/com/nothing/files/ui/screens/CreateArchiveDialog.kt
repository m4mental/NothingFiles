package com.nothing.files.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.nothing.files.engine.ArchiveEngine
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateArchiveDialog(
    targetFiles: List<FileItem>,
    currentDirectory: String,
    onDismiss: () -> Unit,
    onArchiveCreated: () -> Unit
) {
    if (targetFiles.isEmpty()) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val archiveEngine = remember { ArchiveEngine() }

    val defaultArchiveName = remember {
        if (targetFiles.size == 1) {
            val name = File(targetFiles.first().path).nameWithoutExtension
            "$name.zip"
        } else {
            "Archive_${System.currentTimeMillis() % 10000}.zip"
        }
    }

    var archiveName by remember { mutableStateOf(defaultArchiveName) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }

    fun buildArchive() {
        val cleanName = if (archiveName.endsWith(".zip", ignoreCase = true)) archiveName.trim() else "${archiveName.trim()}.zip"
        if (cleanName.isBlank()) {
            Toast.makeText(context, "Please enter an archive name", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        scope.launch {
            val outputFile = File(currentDirectory, cleanName)
            val filesToArchive = targetFiles.map { File(it.path) }
            val ok = archiveEngine.createZip(
                sourceFiles = filesToArchive,
                outputZipFile = outputFile,
                onProgress = { current, total, curFile ->
                    progressStatus = "$current/$total: $curFile"
                }
            )
            isProcessing = false
            if (ok) {
                Toast.makeText(context, "Archive created: $cleanName", Toast.LENGTH_SHORT).show()
                onArchiveCreated()
                onDismiss()
            } else {
                Toast.makeText(context, "Failed to create archive", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BasicAlertDialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
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
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Archive, contentDescription = null, tint = NothingRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Archive (.zip)",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NothingWhite
                        )
                    }

                    if (!isProcessing) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingTextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${targetFiles.size} items to compress into standard ZIP archive",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = NothingTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = archiveName,
                    onValueChange = { archiveName = it },
                    label = { Text("Archive Name") },
                    singleLine = true,
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = NothingBorder,
                        focusedTextColor = NothingWhite,
                        unfocusedTextColor = NothingWhite,
                        focusedContainerColor = NothingBlack,
                        unfocusedContainerColor = NothingBlack
                    )
                )

                if (isProcessing && progressStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = progressStatus,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingTextSecondary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL", color = NothingTextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TextButton(
                        onClick = { buildArchive() },
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = "CREATE ZIP",
                            color = NothingRed,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
