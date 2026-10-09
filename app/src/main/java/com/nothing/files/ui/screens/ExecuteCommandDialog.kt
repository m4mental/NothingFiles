package com.nothing.files.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.RootShellEngine
import com.nothing.files.engine.ShellResult
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecuteCommandDialog(
    initialDirectory: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var command by remember { mutableStateOf("ls -la") }
    var runAsRoot by remember { mutableStateOf(true) }
    var isRunning by remember { mutableStateOf(false) }
    var shellResult by remember { mutableStateOf<ShellResult?>(null) }
    var isRootAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isRootAvailable = RootShellEngine.isRootAvailable()
        runAsRoot = isRootAvailable
    }

    fun runCommand() {
        if (command.isBlank() || isRunning) return
        isRunning = true
        scope.launch {
            val cmd = "cd \"$initialDirectory\" && $command"
            val result = RootShellEngine.executeCommand(cmd, runAsRoot = runAsRoot)
            shellResult = result
            isRunning = false
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        NothingCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = NothingRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "EXECUTE",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NothingWhite
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Directory: $initialDirectory",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NothingTextSecondary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Command Input Field
                OutlinedTextField(
                    value = command,
                    onValueChange = { command = it },
                    label = { Text("Command", fontFamily = FontFamily.Monospace) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NothingRed,
                        unfocusedBorderColor = NothingBorder,
                        focusedTextColor = NothingWhite,
                        unfocusedTextColor = NothingWhite,
                        focusedContainerColor = NothingBlack,
                        unfocusedContainerColor = NothingBlack
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Run as root toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Run as Root (su)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = if (isRootAvailable) NothingWhite else NothingTextSecondary
                        )
                        if (isRootAvailable) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROOTED",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LedOnRed
                            )
                        }
                    }

                    Switch(
                        checked = runAsRoot,
                        onCheckedChange = { runAsRoot = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NothingWhite,
                            checkedTrackColor = NothingRed,
                            uncheckedThumbColor = NothingTextSecondary,
                            uncheckedTrackColor = NothingSurfaceElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Command Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ls -la", "id", "df -h", "uname -a").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingSurfaceElevated)
                                .border(1.dp, NothingBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = NothingWhite,
                                modifier = androidx.compose.ui.Modifier.androidxClickable {
                                    command = preset
                                }
                            )
                        }
                    }
                }

                // Console Output
                shellResult?.let { result ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NothingBlack)
                            .border(1.dp, if (result.exitCode == 0) NothingBorder else NothingRed, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            Text(
                                text = "Exit code: ${result.exitCode} (${if (result.isRoot) "root" else "user"})",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (result.exitCode == 0) LedOnRed else NothingRed
                            )
                            if (result.stdout.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.stdout,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = NothingWhite
                                )
                            }
                            if (result.stderr.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result.stderr,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = NothingRed
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CLOSE", color = NothingTextSecondary, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { runCommand() },
                        enabled = !isRunning
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NothingRed,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NothingRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RUN", color = NothingRed, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.androidxClickable(onClick: () -> Unit): Modifier = this.then(
    Modifier.clickable(onClick = onClick)
)
