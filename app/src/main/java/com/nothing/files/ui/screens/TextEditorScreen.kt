package com.nothing.files.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun TextEditorScreen(
    fileItem: FileItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var content by remember { mutableStateOf("") }
    var originalContent by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(fileItem.path) {
        withContext(Dispatchers.IO) {
            try {
                val text = File(fileItem.path).readText(Charsets.UTF_8)
                content = text
                originalContent = text
            } catch (e: Exception) {
                content = "Error reading file: ${e.message}"
            }
            isLoading = false
        }
    }

    val isModified = content != originalContent
    val lineCount = content.lines().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // Top Bar
        NothingCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NothingWhite
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileItem.name,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        color = if (isModified) NothingRed else NothingWhite
                    )
                    Text(
                        text = "$lineCount lines • ${fileItem.formattedSize} • UTF-8",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingTextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            val success = withContext(Dispatchers.IO) {
                                try {
                                    File(fileItem.path).writeText(content, Charsets.UTF_8)
                                    originalContent = content
                                    true
                                } catch (_: Exception) {
                                    false
                                }
                            }
                            isSaving = false
                            if (success) {
                                Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = isModified && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = NothingRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (isModified) NothingRed else NothingTextSecondary.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
            }
        } else {
            // Editor Area with Line Numbers
            val verticalScroll = rememberScrollState()
            val horizontalScroll = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // Line Numbers Column
                Column(
                    modifier = Modifier
                        .background(NothingSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount.coerceAtLeast(1)) {
                        Text(
                            text = i.toString(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NothingTextSecondary.copy(alpha = 0.5f),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Editable text field
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NothingWhite,
                        lineHeight = 18.sp
                    ),
                    cursorBrush = SolidColor(NothingRed)
                )
            }
        }
    }
}
