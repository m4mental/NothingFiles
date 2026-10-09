package com.nothing.files.engine

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ServerLogMessage(
    val timestamp: Long = System.currentTimeMillis(),
    val ip: String,
    val method: String,
    val path: String,
    val status: Int
)

class LocalHttpServer(
    private val context: Context,
    val port: Int = 8080
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _logs = MutableSharedFlow<ServerLogMessage>(replay = 50)
    val logs = _logs.asSharedFlow()

    fun start() {
        if (isRunning) return
        isRunning = true
        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                while (isRunning) {
                    val clientSocket = serverSocket?.accept() ?: break
                    scope.launch {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    e.printStackTrace()
                }
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
    }

    fun isServerRunning(): Boolean = isRunning

    fun getDeviceIpAddress(): String {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (ipInt != 0) {
                String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            } else {
                "127.0.0.1"
            }
        } catch (_: Exception) {
            "127.0.0.1"
        }
    }

    private fun handleClient(socket: Socket) {
        val clientIp = socket.inetAddress?.hostAddress ?: "Unknown"
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
            val output = BufferedOutputStream(socket.getOutputStream())

            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return

            val method = parts[0]
            val rawPath = parts[1]
            val decodedPath = URLDecoder.decode(rawPath, "UTF-8")

            // Read headers
            val headers = mutableMapOf<String, String>()
            while (true) {
                val headerLine = reader.readLine() ?: break
                if (headerLine.isEmpty()) break
                val idx = headerLine.indexOf(':')
                if (idx != -1) {
                    headers[headerLine.substring(0, idx).trim().lowercase()] = headerLine.substring(idx + 1).trim()
                }
            }

            val storageRoot = Environment.getExternalStorageDirectory()
            val relative = decodedPath.removePrefix("/").substringBefore("?")
            val targetFile = if (relative.isEmpty()) storageRoot else File(storageRoot, relative)

            if (targetFile.isDirectory) {
                val html = generateDirectoryHtml(targetFile, relative)
                sendResponse(output, 200, "OK", "text/html; charset=utf-8", html.toByteArray(StandardCharsets.UTF_8))
                emitLog(clientIp, method, decodedPath, 200)
            } else if (targetFile.exists() && targetFile.isFile) {
                sendFileResponse(output, targetFile)
                emitLog(clientIp, method, decodedPath, 200)
            } else {
                val notFound = "<h1>404 Not Found - Nothing Files</h1>"
                sendResponse(output, 404, "Not Found", "text/html", notFound.toByteArray(StandardCharsets.UTF_8))
                emitLog(clientIp, method, decodedPath, 404)
            }
        } catch (e: Exception) {
            // Socket errors are normal on quick disconnect
        } finally {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun emitLog(ip: String, method: String, path: String, status: Int) {
        scope.launch {
            _logs.emit(ServerLogMessage(ip = ip, method = method, path = path, status = status))
        }
    }

    private fun sendResponse(out: OutputStream, code: Int, status: String, mime: String, data: ByteArray) {
        val header = "HTTP/1.1 $code $status\r\n" +
                "Content-Type: $mime\r\n" +
                "Content-Length: ${data.size}\r\n" +
                "Connection: close\r\n" +
                "Access-Control-Allow-Origin: *\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))
        out.write(data)
        out.flush()
    }

    private fun sendFileResponse(out: OutputStream, file: File) {
        val mime = getMimeType(file)
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: $mime\r\n" +
                "Content-Length: ${file.length()}\r\n" +
                "Content-Disposition: attachment; filename=\"${file.name}\"\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(StandardCharsets.UTF_8))

        FileInputStream(file).use { fis ->
            val buf = ByteArray(64 * 1024)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) {
                out.write(buf, 0, n)
            }
        }
        out.flush()
    }

    private fun getMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "pdf" -> "application/pdf"
            "apk" -> "application/vnd.android.package-archive"
            "zip" -> "application/zip"
            "txt", "log" -> "text/plain"
            "json" -> "application/json"
            else -> "application/octet-stream"
        }
    }

    private fun generateDirectoryHtml(dir: File, relativePath: String): String {
        val files = dir.listFiles()?.sortedWith(
            compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() }
        ) ?: emptyList()

        val parentPath = if (relativePath.isNotEmpty()) {
            val parentRel = File(relativePath).parent ?: ""
            "/$parentRel"
        } else null

        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset='utf-8'><title>NOTHING FILES // WEB SHARE</title>")
        sb.append("<meta name='viewport' content='width=device-width, initial-scale=1'>")
        sb.append("<style>")
        sb.append("body { background-color: #000000; color: #ffffff; font-family: -apple-system, monospace, sans-serif; padding: 20px; }")
        sb.append(".header { border-bottom: 2px solid #D71921; padding-bottom: 12px; margin-bottom: 20px; }")
        sb.append(".brand { font-size: 22px; font-weight: bold; letter-spacing: 2px; color: #D71921; }")
        sb.append(".path { color: #888; font-size: 14px; margin-top: 5px; word-break: break-all; }")
        sb.append(".file-list { display: flex; flex-direction: column; gap: 8px; }")
        sb.append(".file-row { display: flex; justify-content: space-between; align-items: center; background: #0c0c0c; border: 1px solid #222; border-radius: 8px; padding: 12px 16px; text-decoration: none; color: #fff; transition: all 0.2s; }")
        sb.append(".file-row:hover { background: #161616; border-color: #D71921; }")
        sb.append(".file-name { font-weight: 500; font-size: 15px; }")
        sb.append(".badge-dir { color: #D71921; font-weight: bold; margin-right: 8px; }")
        sb.append(".file-size { color: #666; font-size: 13px; font-family: monospace; }")
        sb.append(".back-btn { display: inline-block; background: #1a1a1a; color: #fff; border: 1px solid #333; padding: 8px 16px; border-radius: 6px; text-decoration: none; margin-bottom: 15px; font-weight: bold; }")
        sb.append("</style></head><body>")

        sb.append("<div class='header'>")
        sb.append("<div class='brand'>NOTHING FILES (2.0)</div>")
        sb.append("<div class='path'>PATH: /${relativePath.ifEmpty { "STORAGE" }}</div>")
        sb.append("</div>")

        if (parentPath != null) {
            sb.append("<a class='back-btn' href='$parentPath'>&larr; UP TO PARENT</a>")
        }

        sb.append("<div class='file-list'>")
        for (f in files) {
            val fileRel = if (relativePath.isEmpty()) f.name else "$relativePath/${f.name}"
            val encodedHref = "/" + fileRel.split("/").joinToString("/") { URLEncoder.encode(it, "UTF-8") }
            val isDir = f.isDirectory
            val tag = if (isDir) "<span class='badge-dir'>[DIR]</span>" else ""
            val sizeStr = if (isDir) "" else formatFileSize(f.length())

            sb.append("<a class='file-row' href='$encodedHref'>")
            sb.append("<span class='file-name'>$tag${f.name}</span>")
            sb.append("<span class='file-size'>$sizeStr</span>")
            sb.append("</a>")
        }
        sb.append("</div>")

        sb.append("</body></html>")
        return sb.toString()
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        val value = size / Math.pow(1024.0, digitGroups.toDouble())
        return String.format("%.1f %s", value, units[digitGroups])
    }
}
