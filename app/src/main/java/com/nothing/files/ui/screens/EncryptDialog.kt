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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.EncryptionEngine
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
fun EncryptDialog(
    targetFiles: List<FileItem>,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    if (targetFiles.isEmpty()) return

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { EncryptionEngine() }

    val firstFile = remember { File(targetFiles.first().path) }
    val isDecrypt = remember { firstFile.extension.equals("enc", ignoreCase = true) || engine.isEncryptedFile(firstFile) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var deleteOriginal by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    fun process() {
        if (password.length < 4) {
            Toast.makeText(context, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isDecrypt && password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        scope.launch {
            var allSuccess = true
            for (item in targetFiles) {
                val src = File(item.path)
                if (isDecrypt) {
                    val outName = if (src.name.endsWith(".enc")) src.name.removeSuffix(".enc") else "${src.name}.decrypted"
                    val dest = File(src.parentFile, outName)
                    val ok = engine.decryptFile(src, dest, password, deleteOriginal)
                    if (!ok) allSuccess = false
                } else {
                    val dest = File(src.parentFile, "${src.name}.enc")
                    val ok = engine.encryptFile(src, dest, password, deleteOriginal)
                    if (!ok) allSuccess = false
                }
            }
            isProcessing = false
            if (allSuccess) {
                Toast.makeText(context, if (isDecrypt) "Decrypted successfully" else "AES-256 Encrypted successfully", Toast.LENGTH_SHORT).show()
                onSuccess()
                onDismiss()
            } else {
                Toast.makeText(context, "Operation failed or incorrect password", Toast.LENGTH_LONG).show()
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
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NothingRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDecrypt) "Decrypt File" else "AES-256 Encrypt",
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
                    text = "${targetFiles.size} items selected",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = NothingTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Enter Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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

                if (!isDecrypt) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
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
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = deleteOriginal,
                        onCheckedChange = { deleteOriginal = it },
                        enabled = !isProcessing,
                        colors = CheckboxDefaults.colors(
                            checkedColor = NothingRed,
                            uncheckedColor = NothingTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDecrypt) "Delete .enc after decrypting" else "Delete original after encrypting",
                        fontSize = 12.sp,
                        color = NothingWhite
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("CANCEL", color = NothingTextSecondary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TextButton(
                        onClick = { process() },
                        enabled = !isProcessing
                    ) {
                        Text(
                            text = if (isDecrypt) "DECRYPT" else "ENCRYPT",
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
