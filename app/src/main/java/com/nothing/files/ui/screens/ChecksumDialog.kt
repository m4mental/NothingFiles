package com.nothing.files.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nothing.files.engine.ChecksumEngine
import com.nothing.files.engine.ChecksumResult
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import java.io.File

@Composable
fun ChecksumDialog(
    fileItem: FileItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var result by remember { mutableStateOf<ChecksumResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(fileItem.path) {
        val engine = ChecksumEngine()
        result = engine.calculateChecksums(File(fileItem.path))
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
                Text(
                    text = "CHECKSUM // HASH",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.2.sp,
                    color = NothingRed
                )

                Text(
                    text = fileItem.name,
                    maxLines = 1,
                    fontSize = 13.sp,
                    color = NothingTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = NothingRed,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CALCULATING...",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NothingWhite
                        )
                    }
                } else {
                    result?.let { res ->
                        ChecksumRow("MD5", res.md5, context)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChecksumRow("SHA-1", res.sha1, context)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChecksumRow("SHA-256", res.sha256, context)
                        Spacer(modifier = Modifier.height(8.dp))
                        ChecksumRow("CRC32", res.crc32, context)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Calculated in ${res.durationMs}ms",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NothingTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CLOSE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NothingWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecksumRow(
    algorithm: String,
    hashValue: String,
    context: Context
) {
    NothingCard(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = NothingSurfaceElevated,
        borderColor = NothingBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = algorithm,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NothingRed
                )
                Text(
                    text = hashValue,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NothingWhite,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(algorithm, hashValue)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "$algorithm copied", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = NothingTextSecondary,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}
