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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsDialog(
    targetFiles: List<FileItem>,
    onDismiss: () -> Unit,
    onTagsUpdated: () -> Unit
) {
    if (targetFiles.isEmpty()) return

    val context = LocalContext.current
    var customTag by remember { mutableStateOf("") }
    val defaultColors = listOf(
        Pair("Red", Color(0xFFD71921)),
        Pair("Orange", Color(0xFFFF9800)),
        Pair("Yellow", Color(0xFFFFEB3B)),
        Pair("Green", Color(0xFF4CAF50)),
        Pair("Cyan", Color(0xFF00E5FF)),
        Pair("Purple", Color(0xFF9C27B0))
    )
    var selectedColor by remember { mutableStateOf<String?>(null) }

    fun saveTag() {
        val tagToApply = if (customTag.isNotBlank()) customTag.trim() else selectedColor ?: "Tagged"
        val prefs = context.getSharedPreferences("nothing_files_tags", android.content.Context.MODE_PRIVATE)
        val editor = prefs.edit()
        targetFiles.forEach {
            editor.putString(it.path, tagToApply)
        }
        editor.apply()
        Toast.makeText(context, "Tag '$tagToApply' applied to ${targetFiles.size} items", Toast.LENGTH_SHORT).show()
        onTagsUpdated()
        onDismiss()
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tag, contentDescription = null, tint = NothingRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Tags",
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NothingWhite
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingTextSecondary, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select a Color Tag:",
                    fontSize = 12.sp,
                    color = NothingTextSecondary,
                    fontFamily = FontFamily.Default
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    defaultColors.forEach { (name, color) ->
                        val isSelected = selectedColor == name
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) NothingWhite else NothingBlack,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = name }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = customTag,
                    onValueChange = { customTag = it },
                    label = { Text("Or enter Custom Tag (e.g. Work, Secret)") },
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

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = NothingTextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TextButton(onClick = { saveTag() }) {
                        Text("SAVE TAG", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
