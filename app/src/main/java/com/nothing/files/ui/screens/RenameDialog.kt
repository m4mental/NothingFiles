package com.nothing.files.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch
import java.io.File

enum class RenameMode {
    NAME,
    FULL_NAME,
    EXTENSION,
    REGEX
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialog(
    targetFiles: List<FileItem>,
    onDismiss: () -> Unit,
    onRenamed: () -> Unit
) {
    if (targetFiles.isEmpty()) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { FileManagerEngine() }

    val primaryFile = targetFiles.first()
    val fileObj = remember { File(primaryFile.path) }
    val initialExtension = remember { fileObj.extension }
    val initialBaseName = remember { if (fileObj.isDirectory) fileObj.name else fileObj.nameWithoutExtension }

    var renameMode by remember { mutableStateOf(RenameMode.NAME) }
    var inputName by remember {
        mutableStateOf(
            if (fileObj.isDirectory) fileObj.name else fileObj.nameWithoutExtension
        )
    }

    var showAdvanced by remember { mutableStateOf(false) }
    var regexFind by remember { mutableStateOf("") }
    var regexReplace by remember { mutableStateOf("") }
    var preserveExtension by remember { mutableStateOf(true) }

    fun executeRename() {
        scope.launch {
            if (targetFiles.size == 1) {
                val newFinalName = when (renameMode) {
                    RenameMode.NAME -> {
                        if (fileObj.isDirectory || initialExtension.isEmpty()) inputName.trim()
                        else "${inputName.trim()}.$initialExtension"
                    }
                    RenameMode.FULL_NAME -> inputName.trim()
                    RenameMode.EXTENSION -> {
                        val ext = inputName.trim().removePrefix(".")
                        if (ext.isEmpty()) initialBaseName else "$initialBaseName.$ext"
                    }
                    RenameMode.REGEX -> {
                        if (regexFind.isEmpty()) fileObj.name
                        else fileObj.name.replace(Regex(regexFind), regexReplace)
                    }
                }

                if (newFinalName.isEmpty()) {
                    Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val ok = engine.renameFile(fileObj, newFinalName)
                if (ok) {
                    Toast.makeText(context, "Renamed to $newFinalName", Toast.LENGTH_SHORT).show()
                    onRenamed()
                    onDismiss()
                } else {
                    Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Batch rename multiple files
                val count = engine.batchRename(
                    files = targetFiles.map { File(it.path) },
                    prefix = if (renameMode == RenameMode.NAME) inputName else "",
                    replaceFind = regexFind,
                    replaceWith = regexReplace,
                    includeIndex = true,
                    startIndex = 1
                )
                Toast.makeText(context, "Renamed $count files", Toast.LENGTH_SHORT).show()
                onRenamed()
                onDismiss()
            }
        }
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
                    .padding(20.dp)
            ) {
                // Title Row (Matching Screenshot 4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (targetFiles.size > 1) "Rename (${targetFiles.size} items)" else "Rename",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NothingWhite
                    )

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

                // Input Section (Screenshot 4)
                Text(
                    text = "Enter the name",
                    fontFamily = FontFamily.Default,
                    fontSize = 12.sp,
                    color = NothingTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = NothingTextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = NothingBorder,
                        focusedTextColor = NothingWhite,
                        unfocusedTextColor = NothingWhite,
                        cursorColor = NothingRed,
                        focusedContainerColor = NothingBlack,
                        unfocusedContainerColor = NothingBlack
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Radio Buttons (Name | Full name / Extension | Regex)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                renameMode = RenameMode.NAME
                                inputName = if (fileObj.isDirectory) fileObj.name else fileObj.nameWithoutExtension
                            }
                    ) {
                        RadioButton(
                            selected = renameMode == RenameMode.NAME,
                            onClick = {
                                renameMode = RenameMode.NAME
                                inputName = if (fileObj.isDirectory) fileObj.name else fileObj.nameWithoutExtension
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NothingRed,
                                unselectedColor = NothingTextSecondary
                            )
                        )
                        Text(
                            text = "Name",
                            fontSize = 14.sp,
                            color = if (renameMode == RenameMode.NAME) NothingWhite else NothingTextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                renameMode = RenameMode.FULL_NAME
                                inputName = fileObj.name
                            }
                    ) {
                        RadioButton(
                            selected = renameMode == RenameMode.FULL_NAME,
                            onClick = {
                                renameMode = RenameMode.FULL_NAME
                                inputName = fileObj.name
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NothingRed,
                                unselectedColor = NothingTextSecondary
                            )
                        )
                        Text(
                            text = "Full name",
                            fontSize = 14.sp,
                            color = if (renameMode == RenameMode.FULL_NAME) NothingWhite else NothingTextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                renameMode = RenameMode.EXTENSION
                                inputName = initialExtension
                            }
                    ) {
                        RadioButton(
                            selected = renameMode == RenameMode.EXTENSION,
                            onClick = {
                                renameMode = RenameMode.EXTENSION
                                inputName = initialExtension
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NothingRed,
                                unselectedColor = NothingTextSecondary
                            )
                        )
                        Text(
                            text = "Extension",
                            fontSize = 14.sp,
                            color = if (renameMode == RenameMode.EXTENSION) NothingWhite else NothingTextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { renameMode = RenameMode.REGEX }
                    ) {
                        RadioButton(
                            selected = renameMode == RenameMode.REGEX,
                            onClick = { renameMode = RenameMode.REGEX },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NothingRed,
                                unselectedColor = NothingTextSecondary
                            )
                        )
                        Text(
                            text = "Regex",
                            fontSize = 14.sp,
                            color = if (renameMode == RenameMode.REGEX) NothingWhite else NothingTextSecondary
                        )
                    }
                }

                // Advanced Settings Expander (Screenshot 4)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showAdvanced = !showAdvanced }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (showAdvanced) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Advanced settings",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = NothingWhite
                    )
                }

                AnimatedVisibility(visible = showAdvanced || renameMode == RenameMode.REGEX) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = regexFind,
                            onValueChange = { regexFind = it },
                            label = { Text("Find pattern / string", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = regexReplace,
                            onValueChange = { regexReplace = it },
                            label = { Text("Replace with", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedTextColor = NothingWhite,
                                unfocusedTextColor = NothingWhite
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Buttons (CANCEL | OK)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NothingTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TextButton(onClick = { executeRename() }) {
                        Text(
                            text = "OK",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NothingRed
                        )
                    }
                }
            }
        }
    }
}
