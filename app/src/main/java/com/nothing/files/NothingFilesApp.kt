package com.nothing.files

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class NothingFilesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_HTTP_SERVER,
                "Web Sharing Server",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Foreground service notification for Nothing Files Wi-Fi sharing"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_HTTP_SERVER = "nothing_files_server_channel"
    }
}
