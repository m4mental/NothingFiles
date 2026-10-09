package com.nothing.files.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

data class HexLine(
    val offset: String,
    val hexPart1: String,
    val hexPart2: String,
    val ascii: String
)

@Composable
fun HexViewerScreen(
    fileItem: FileItem,
    onBack: () -> Unit
) {
    var hexLines by remember { mutableStateOf<List<HexLine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(fileItem.path) {
        withContext(Dispatchers.IO) {
            val lines = mutableListOf<HexLine>()
            try {
                val file = File(fileItem.path)
                val buffer = ByteArray(16)
                var offset = 0L
                FileInputStream(file).use { fis ->
                    var bytesRead: Int
                    // Read up to 8KB for fast fluid rendering
                    while (fis.read(buffer).also { bytesRead = it } != -1 && offset < 8192) {
                        val offsetStr = "%08X".format(offset)
                        val hexList = (0 until bytesRead).map { "%02X".format(buffer[it]) }

                        val part1 = hexList.take(8).joinToString(" ")
                        val part2 = hexList.drop(8).joinToString(" ")

                        val ascii = (0 until bytesRead).map { idx ->
                            val b = buffer[idx].toInt()
                            if (b in 32..126) b.toChar() else '.'
                        }.joinToString("")

                        lines.add(
                            HexLine(
                                offset = offsetStr,
                                hexPart1 = part1.padEnd(23),
                                hexPart2 = part2.padEnd(23),
                                ascii = ascii
                            )
                        )
                        offset += bytesRead
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            hexLines = lines
            isLoading = false
        }
    }

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
                        text = "HEX VIEWER // ${fileItem.name}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NothingRed
                    )
                    Text(
                        text = "${fileItem.formattedSize} • Offset (HEX) | Bytes | ASCII",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingTextSecondary
                    )
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
            val horizontalScroll = rememberScrollState()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                itemsIndexed(hexLines) { _, line ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = line.offset,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = NothingRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${line.hexPart1}  ${line.hexPart2}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NothingWhite
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "| ${line.ascii} |",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = NothingTextSecondary
                        )
                    }
                }
            }
        }
    }
}
