package com.nothing.files.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun BatchRenameDialog(
    selectedFiles: List<FileItem>,
    onDismiss: () -> Unit,
    onConfirm: (prefix: String, suffix: String, find: String, replace: String, includeIndex: Boolean) -> Unit
) {
    var prefix by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var includeIndex by remember { mutableStateOf(false) }

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
                Text(
                    text = "BATCH RENAME // ${selectedFiles.size} FILES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.2.sp,
                    color = NothingRed
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prefix,
                        onValueChange = { prefix = it },
                        label = { Text("Prefix", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = suffix,
                        onValueChange = { suffix = it },
                        label = { Text("Suffix", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = findText,
                        onValueChange = { findText = it },
                        label = { Text("Find", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                    OutlinedTextField(
                        value = replaceText,
                        onValueChange = { replaceText = it },
                        label = { Text("Replace", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeIndex,
                        onCheckedChange = { includeIndex = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NothingRed,
                            uncheckedColor = NothingBorder
                        )
                    )
                    Text(
                        text = "Append numbering index (_1, _2...)",
                        fontSize = 13.sp,
                        color = NothingWhite
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Box
                Text(
                    text = "PREVIEW:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NothingTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                NothingCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = NothingSurfaceElevated,
                    borderColor = NothingBorder
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(selectedFiles.take(5)) { file ->
                            val ext = if (file.isDirectory) "" else "." + file.extension
                            val base = if (file.isDirectory) file.name else file.name.substringBeforeLast('.')
                            var newBase = base
                            if (findText.isNotEmpty()) {
                                newBase = newBase.replace(findText, replaceText)
                            }
                            val indexPart = if (includeIndex) "_1" else ""
                            val previewName = "$prefix$newBase$indexPart$suffix$ext"

                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = "${file.name} -> $previewName",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = NothingWhite
                                )
                            }
                        }
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
                            onConfirm(prefix, suffix, findText, replaceText, includeIndex)
                            onDismiss()
                        }
                    ) {
                        Text("RENAME", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NothingRed,
    unfocusedBorderColor = NothingBorder,
    focusedLabelColor = NothingRed,
    unfocusedLabelColor = NothingTextSecondary,
    focusedTextColor = NothingWhite,
    unfocusedTextColor = NothingWhite,
    cursorColor = NothingRed
)
