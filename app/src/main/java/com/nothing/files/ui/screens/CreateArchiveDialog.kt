package com.nothing.files.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.ArchiveConfig
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateArchiveDialog(
    targetFiles: List<FileItem>,
    currentDirectory: String,
    onDismiss: () -> Unit,
    onQueueToClipboard: (ArchiveConfig) -> Unit
) {
    if (targetFiles.isEmpty()) return

    val context = LocalContext.current

    val formats = listOf("Zip", "7z", "Tar", "Gzip", "BZip2", "XZ", "Lizard", "Lz4", "Lz5", "ZStd", "Brotli")
    val compressionMethods = listOf("Deflate", "Store", "BZip2")
    val compressionLevels = listOf("Store", "Fastest", "Fast", "Normal", "Maximum", "Ultra")
    val encryptions = listOf("ZipCrypto", "AES-256")
    val splitUnits = listOf("MiB", "KiB", "GiB")

    var selectedFormat by remember { mutableStateOf("Zip") }
    var selectedMethod by remember { mutableStateOf("Deflate") }
    var selectedLevel by remember { mutableStateOf("Normal") }
    var selectedEncryption by remember { mutableStateOf("ZipCrypto") }

    val baseName = remember {
        if (targetFiles.size == 1) {
            val f = File(targetFiles.first().path)
            f.nameWithoutExtension
        } else {
            val parent = File(currentDirectory).name.ifBlank { "Archive" }
            "${parent}_${System.currentTimeMillis() % 10000}"
        }
    }

    var archiveName by remember { mutableStateOf("$baseName.zip") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberKey by remember { mutableStateOf(false) }
    var deleteSource by remember { mutableStateOf(false) }
    var splitLengthText by remember { mutableStateOf("") }
    var selectedSplitUnit by remember { mutableStateOf("MiB") }
    var comment by remember { mutableStateOf("") }

    // Dropdown expansion states
    var formatExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }
    var encryptExpanded by remember { mutableStateOf(false) }
    var splitUnitExpanded by remember { mutableStateOf(false) }

    fun updateExtensionForFormat(format: String) {
        val ext = when (format.lowercase()) {
            "7z" -> ".7z"
            "tar" -> ".tar"
            "gzip" -> ".tar.gz"
            "bzip2" -> ".tar.bz2"
            "xz" -> ".tar.xz"
            "zstd" -> ".tar.zst"
            else -> ".zip"
        }
        val clean = archiveName.substringBeforeLast('.')
        archiveName = "$clean$ext"
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header (Archive Title + X Close button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Archive",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NothingWhite
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = NothingTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enter the name
                OutlinedTextField(
                    value = archiveName,
                    onValueChange = { archiveName = it },
                    label = { Text("Enter the name", fontSize = 12.sp) },
                    singleLine = true,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Row 1: Format & Compression Method Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Format Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingBlack)
                                .clickable { formatExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedFormat, color = NothingWhite, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NothingTextSecondary)
                        }
                        DropdownMenu(
                            expanded = formatExpanded,
                            onDismissRequest = { formatExpanded = false },
                            modifier = Modifier.background(NothingSurface)
                        ) {
                            formats.forEach { fmt ->
                                DropdownMenuItem(
                                    text = { Text(fmt, color = if (fmt == selectedFormat) NothingRed else NothingWhite) },
                                    onClick = {
                                        selectedFormat = fmt
                                        formatExpanded = false
                                        updateExtensionForFormat(fmt)
                                    }
                                )
                            }
                        }
                    }

                    // Compression Method Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingBlack)
                                .clickable { methodExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedMethod, color = NothingWhite, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NothingTextSecondary)
                        }
                        DropdownMenu(
                            expanded = methodExpanded,
                            onDismissRequest = { methodExpanded = false },
                            modifier = Modifier.background(NothingSurface)
                        ) {
                            compressionMethods.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method, color = if (method == selectedMethod) NothingRed else NothingWhite) },
                                    onClick = {
                                        selectedMethod = method
                                        methodExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Compression Level & Encryption Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Compression Level Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingBlack)
                                .clickable { levelExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedLevel, color = NothingWhite, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NothingTextSecondary)
                        }
                        DropdownMenu(
                            expanded = levelExpanded,
                            onDismissRequest = { levelExpanded = false },
                            modifier = Modifier.background(NothingSurface)
                        ) {
                            compressionLevels.forEach { lvl ->
                                DropdownMenuItem(
                                    text = { Text(lvl, color = if (lvl == selectedLevel) NothingRed else NothingWhite) },
                                    onClick = {
                                        selectedLevel = lvl
                                        levelExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Encryption Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingBlack)
                                .clickable { encryptExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedEncryption, color = NothingWhite, fontSize = 14.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NothingTextSecondary)
                        }
                        DropdownMenu(
                            expanded = encryptExpanded,
                            onDismissRequest = { encryptExpanded = false },
                            modifier = Modifier.background(NothingSurface)
                        ) {
                            encryptions.forEach { enc ->
                                DropdownMenuItem(
                                    text = { Text(enc, color = if (enc == selectedEncryption) NothingRed else NothingWhite) },
                                    onClick = {
                                        selectedEncryption = enc
                                        encryptExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Enter the key (Password)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter the key", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = NothingTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Re-enter password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Re-enter", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                Spacer(modifier = Modifier.height(10.dp))

                // Checkboxes: Remember & Delete source
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { rememberKey = !rememberKey }
                            .padding(end = 16.dp)
                    ) {
                        Checkbox(
                            checked = rememberKey,
                            onCheckedChange = { rememberKey = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NothingRed,
                                uncheckedColor = NothingBorder
                            )
                        )
                        Text("Remember", color = NothingWhite, fontSize = 13.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { deleteSource = !deleteSource }
                    ) {
                        Checkbox(
                            checked = deleteSource,
                            onCheckedChange = { deleteSource = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NothingRed,
                                uncheckedColor = NothingBorder
                            )
                        )
                        Text("Delete source", color = NothingWhite, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Split length (Minimum 64KB) + Unit Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = splitLengthText,
                        onValueChange = { splitLengthText = it },
                        label = { Text("Split length (Minimum 64KB)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NothingRed,
                            unfocusedBorderColor = NothingBorder,
                            focusedTextColor = NothingWhite,
                            unfocusedTextColor = NothingWhite,
                            focusedContainerColor = NothingBlack,
                            unfocusedContainerColor = NothingBlack
                        )
                    )

                    Box(modifier = Modifier.width(90.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingBlack)
                                .clickable { splitUnitExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedSplitUnit, color = NothingWhite, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = NothingTextSecondary)
                        }
                        DropdownMenu(
                            expanded = splitUnitExpanded,
                            onDismissRequest = { splitUnitExpanded = false },
                            modifier = Modifier.background(NothingSurface)
                        ) {
                            splitUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit, color = if (unit == selectedSplitUnit) NothingRed else NothingWhite) },
                                    onClick = {
                                        selectedSplitUnit = unit
                                        splitUnitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment", fontSize = 12.sp) },
                    singleLine = true,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: CANCEL & OK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            color = NothingTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            val cleanName = archiveName.trim()
                            if (cleanName.isBlank()) {
                                Toast.makeText(context, "Please enter an archive name", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            if (password.isNotEmpty() && password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }

                            val finalName = if (!cleanName.contains('.')) {
                                when (selectedFormat.lowercase()) {
                                    "7z" -> "$cleanName.7z"
                                    "tar" -> "$cleanName.tar"
                                    "gzip" -> "$cleanName.tar.gz"
                                    "bzip2" -> "$cleanName.tar.bz2"
                                    else -> "$cleanName.zip"
                                }
                            } else cleanName

                            val splitBytes = splitLengthText.toLongOrNull()?.let { len ->
                                when (selectedSplitUnit) {
                                    "KiB" -> len * 1024L
                                    "GiB" -> len * 1024L * 1024L * 1024L
                                    else -> len * 1024L * 1024L // MiB
                                }
                            } ?: 0L

                            val config = ArchiveConfig(
                                archiveName = finalName,
                                format = selectedFormat,
                                compressionMethod = selectedMethod,
                                compressionLevel = selectedLevel,
                                encryption = selectedEncryption,
                                password = password,
                                rememberKey = rememberKey,
                                deleteSource = deleteSource,
                                splitLength = splitBytes,
                                splitUnit = selectedSplitUnit,
                                comment = comment
                            )

                            // Queue configured archive task to clipboard as requested!
                            onQueueToClipboard(config)
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "OK",
                            color = NothingRed,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
