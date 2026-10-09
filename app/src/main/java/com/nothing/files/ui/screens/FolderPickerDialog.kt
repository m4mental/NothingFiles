package com.nothing.files.ui.screens

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.FileManagerEngine
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerDialog(
    title: String = "Select Destination",
    targetFiles: List<FileItem>,
    isMove: Boolean = false,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { FileManagerEngine() }

    val defaultPath = remember { Environment.getExternalStorageDirectory().absolutePath }
    var currentPath by remember { mutableStateOf(defaultPath) }
    var subfolders by remember { mutableStateOf<List<File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isOperating by remember { mutableStateOf(false) }

    var showCreateFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    fun loadFolders(path: String) {
        isLoading = true
        scope.launch {
            val list = withContext(Dispatchers.IO) {
                val dir = File(path)
                dir.listFiles { f -> f.isDirectory && !f.name.startsWith(".") }?.sortedBy { it.name.lowercase() } ?: emptyList()
            }
            subfolders = list
            isLoading = false
        }
    }

    LaunchedEffect(currentPath) {
        loadFolders(currentPath)
    }

    fun executeOperation() {
        isOperating = true
        scope.launch {
            val destDir = File(currentPath)
            val filesList = targetFiles.map { File(it.path) }
            val ok = if (isMove) {
                engine.moveFiles(filesList, destDir)
            } else {
                engine.copyFiles(filesList, destDir)
            }
            isOperating = false
            if (ok) {
                Toast.makeText(context, "${if (isMove) "Moved" else "Copied"} to ${destDir.name}", Toast.LENGTH_SHORT).show()
                onComplete()
                onDismiss()
            } else {
                Toast.makeText(context, "Error copying/moving files", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BasicAlertDialog(onDismissRequest = { if (!isOperating) onDismiss() }) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 580.dp)
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = NothingWhite
                        )
                        Text(
                            text = currentPath.substringAfterLast('/'),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NothingRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        IconButton(onClick = { showCreateFolder = !showCreateFolder }) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = NothingWhite)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingTextSecondary)
                        }
                    }
                }

                if (showCreateFolder) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = { Text("New folder name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(
                            onClick = {
                                if (newFolderName.isNotBlank()) {
                                    scope.launch {
                                        engine.createFolder(File(currentPath), newFolderName.trim())
                                        newFolderName = ""
                                        showCreateFolder = false
                                        loadFolders(currentPath)
                                    }
                                }
                            }
                        ) {
                            Text("ADD", color = NothingRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = NothingBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Navigation Controls (Back to parent)
                if (currentPath != defaultPath && currentPath != "/") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                val parent = File(currentPath).parent
                                if (parent != null) currentPath = parent
                            }
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Parent folder",
                            tint = NothingRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ".. (Parent directory)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = NothingRed
                        )
                    }
                }

                // Folder List
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
                        }
                    } else if (subfolders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                            Text("NO SUBFOLDERS", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingTextSecondary)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(subfolders, key = { it.absolutePath }) { folder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { currentPath = folder.absolutePath }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = NothingWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = folder.name,
                                        fontFamily = FontFamily.Default,
                                        fontSize = 14.sp,
                                        color = NothingWhite,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = NothingBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Bottom Confirmation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !isOperating) {
                        Text("CANCEL", color = NothingTextSecondary, fontWeight = FontWeight.Bold)
                    }

                    if (isOperating) {
                        CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        TextButton(onClick = { executeOperation() }) {
                            Text(
                                text = if (isMove) "MOVE HERE" else "COPY HERE",
                                color = NothingRed,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
