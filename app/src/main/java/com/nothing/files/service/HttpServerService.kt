package com.nothing.files.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.nothing.files.MainActivity
import com.nothing.files.NothingFilesApp
import com.nothing.files.engine.LocalHttpServer

class HttpServerService : Service() {

    private var server: LocalHttpServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val port = intent?.getIntExtra("PORT", 8080) ?: 8080
        val action = intent?.action

        if (action == ACTION_STOP) {
            stopServer()
            stopSelf()
            return START_NOT_STICKY
        }

        startServer(port)
        return START_STICKY
    }

    private fun startServer(port: Int) {
        if (server == null) {
            server = LocalHttpServer(this, port)
            activeServerInstance = server
            server?.start()
        }

        val ip = server?.getDeviceIpAddress() ?: "127.0.0.1"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, NothingFilesApp.CHANNEL_HTTP_SERVER)
            .setContentTitle("NOTHING FILES // WEB SHARE")
            .setContentText("Listening on http://$ip:$port")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun stopServer() {
        server?.stop()
        server = null
        activeServerInstance = null
    }

    override fun onDestroy() {
        stopServer()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.nothing.files.START_SERVER"
        const val ACTION_STOP = "com.nothing.files.STOP_SERVER"

        var activeServerInstance: LocalHttpServer? = null
    }
}
