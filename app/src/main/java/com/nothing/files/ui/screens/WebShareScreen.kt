package com.nothing.files.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.LocalHttpServer
import com.nothing.files.engine.ServerLogMessage
import com.nothing.files.service.HttpServerService
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.LedOff
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingAccentGreen
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WebShareScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isRunning by remember {
        mutableStateOf(HttpServerService.activeServerInstance?.isServerRunning() == true)
    }

    val serverInstance = HttpServerService.activeServerInstance
    val ip = serverInstance?.getDeviceIpAddress() ?: remember {
        val s = LocalHttpServer(context)
        s.getDeviceIpAddress()
    }
    val port = serverInstance?.port ?: 8080
    val url = "http://$ip:$port"

    val logs = remember { mutableStateListOf<ServerLogMessage>() }

    LaunchedEffect(isRunning) {
        val active = HttpServerService.activeServerInstance
        if (active != null) {
            active.logs.collectLatest { log ->
                logs.add(0, log)
                if (logs.size > 100) logs.removeAt(logs.size - 1)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // Top Header
        NothingCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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

                Column {
                    Text(
                        text = "WEB SHARE // HTTP SERVER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.2.sp,
                        color = NothingRed
                    )
                    Text(
                        text = "Wireless PC & Mobile file transfer",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingTextSecondary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Main Status Card
            NothingCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = NothingSurface,
                borderColor = if (isRunning) NothingRed else NothingBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsating LED indicator
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (isRunning) Modifier.alpha(alphaAnim) else Modifier)
                        ) {
                            drawCircle(color = if (isRunning) LedOnRed else LedOff)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isRunning) "SERVER ACTIVE" else "SERVER STOPPED",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp,
                        color = if (isRunning) NothingWhite else NothingTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isRunning) {
                        NothingCard(
                            shape = RoundedCornerShape(10.dp),
                            backgroundColor = NothingSurfaceElevated,
                            borderColor = NothingRed
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = url,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = NothingRed
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Nothing Share URL", url))
                                        Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = NothingWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Open $url in your browser to access Nothing Files")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share URL"))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = NothingWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Connect PC or phone to the same Wi-Fi and open the link above in your web browser.",
                            fontSize = 12.sp,
                            color = NothingTextSecondary,
                            lineHeight = 16.sp
                        )
                    } else {
                        Text(
                            text = "Start the embedded HTTP server to wirelessly transfer, download, and stream files to your PC or laptop without cables.",
                            fontSize = 12.sp,
                            color = NothingTextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Start/Stop Action Button
                    Button(
                        onClick = {
                            val intent = Intent(context, HttpServerService::class.java).apply {
                                action = if (isRunning) HttpServerService.ACTION_STOP else HttpServerService.ACTION_START
                                putExtra("PORT", port)
                            }
                            if (isRunning) {
                                context.startService(intent)
                                isRunning = false
                            } else {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                                isRunning = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) NothingSurfaceElevated else NothingRed,
                            contentColor = NothingWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isRunning) "STOP SERVER" else "START SERVER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Connection Terminal Log
            Text(
                text = "LIVE ACCESS TERMINAL:",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = NothingTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            NothingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                backgroundColor = NothingSurfaceElevated,
                borderColor = NothingBorder
            ) {
                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isRunning) "Listening for connections..." else "Server inactive",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NothingTextSecondary
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.padding(10.dp)) {
                        items(logs) { log ->
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$timeStr  ${log.ip}  ${log.method} ${log.path}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = NothingWhite,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "[${log.status}]",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.status == 200) NothingAccentGreen else NothingRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
