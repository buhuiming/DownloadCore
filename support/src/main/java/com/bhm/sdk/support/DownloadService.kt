package com.bhm.sdk.support

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

/**
 * @author Buhuiming
 * @description:
 * @date :2022/10/24 18:02
 */
internal class DownloadService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        private var serviceId = 10001

        private var notification: Notification? = null

        fun start(context: Context, notification: Notification?, notificationId: Int?) {
            this.notification = notification
            notificationId?.let { it -> this.serviceId = it }
            val intent = Intent(context, DownloadService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun destroy(context: Context) {
            val intent = Intent(context, DownloadService::class.java)
            context.stopService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification(notification)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun showNotification(notification: Notification?) {
        notification?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(serviceId, it)
            } else {
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(serviceId, it)
            }
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(true)
        }
        stopSelf()
        super.onDestroy()
    }
}