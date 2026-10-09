package com.nothing.files.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.window.Dialog
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
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

@Composable
fun PermissionsDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val targetFile = remember { File(fileItem.path) }

    var readOwner by remember { mutableStateOf(targetFile.canRead()) }
    var writeOwner by remember { mutableStateOf(targetFile.canWrite()) }
    var execOwner by remember { mutableStateOf(targetFile.canExecute()) }

    var readGroup by remember { mutableStateOf(true) }
    var writeGroup by remember { mutableStateOf(false) }
    var execGroup by remember { mutableStateOf(targetFile.canExecute()) }

    var readOther by remember { mutableStateOf(true) }
    var writeOther by remember { mutableStateOf(false) }
    var execOther by remember { mutableStateOf(targetFile.canExecute()) }

    val ownerOctal = (if (readOwner) 4 else 0) + (if (writeOwner) 2 else 0) + (if (execOwner) 1 else 0)
    val groupOctal = (if (readGroup) 4 else 0) + (if (writeGroup) 2 else 0) + (if (execGroup) 1 else 0)
    val otherOctal = (if (readOther) 4 else 0) + (if (writeOther) 2 else 0) + (if (execOther) 1 else 0)
    val chmodCode = "$ownerOctal$groupOctal$otherOctal"

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERMISSIONS // CHMOD",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.2.sp,
                        color = NothingRed
                    )
                    Text(
                        text = chmodCode,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NothingWhite
                    )
                }

                Text(
                    text = fileItem.name,
                    maxLines = 1,
                    fontSize = 12.sp,
                    color = NothingTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                NothingCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = NothingSurfaceElevated,
                    borderColor = NothingBorder
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CATEGORY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingTextSecondary)
                            Text("R", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingRed)
                            Text("W", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingRed)
                            Text("X", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingRed)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Owner Row
                        PermissionRow("OWNER", readOwner, { readOwner = it }, writeOwner, { writeOwner = it }, execOwner, { execOwner = it })
                        PermissionRow("GROUP", readGroup, { readGroup = it }, writeGroup, { writeGroup = it }, execGroup, { execGroup = it })
                        PermissionRow("OTHER", readOther, { readOther = it }, writeOther, { writeOther = it }, execOther, { execOther = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }

                    TextButton(
                        onClick = {
                            scope.launch {
                                val success = withContext(Dispatchers.IO) {
                                    try {
                                        val shellRes = com.nothing.files.engine.RootShellEngine.executeCommand(
                                            "chmod $chmodCode \"${targetFile.absolutePath}\"",
                                            runAsRoot = true
                                        )
                                        if (shellRes.exitCode == 0) {
                                            true
                                        } else {
                                            targetFile.setReadable(readOwner, false)
                                            targetFile.setWritable(writeOwner, false)
                                            targetFile.setExecutable(execOwner, false)
                                            true
                                        }
                                    } catch (_: Exception) {
                                        try {
                                            targetFile.setReadable(readOwner, false)
                                            targetFile.setWritable(writeOwner, false)
                                            targetFile.setExecutable(execOwner, false)
                                            true
                                        } catch (_: Exception) {
                                            false
                                        }
                                    }
                                }
                                if (success) {
                                    Toast.makeText(context, "Permissions updated (chmod $chmodCode)", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Failed to apply permissions", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) {
                        Text("APPLY", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    r: Boolean, onR: (Boolean) -> Unit,
    w: Boolean, onW: (Boolean) -> Unit,
    x: Boolean, onX: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingWhite)
        Checkbox(checked = r, onCheckedChange = onR, colors = checkboxColors())
        Checkbox(checked = w, onCheckedChange = onW, colors = checkboxColors())
        Checkbox(checked = x, onCheckedChange = onX, colors = checkboxColors())
    }
}

@Composable
private fun checkboxColors() = CheckboxDefaults.colors(
    checkedColor = NothingRed,
    uncheckedColor = NothingBorder
)
