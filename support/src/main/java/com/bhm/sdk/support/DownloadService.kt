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
class DownloadService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    private val serviceId = 111111

    companion object {

        private var notification: Notification? = null

        fun start(context: Context, notification: Notification?) {
            this.notification = notification
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
        notification?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(serviceId, it)
            } else {
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(serviceId, it)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(true)
        }
        stopSelf()
        super.onDestroy()
    }
}