package com.bhm.sdk.support

import android.app.Application
import android.app.Notification
import com.bhm.sdk.support.interfaces.IRequest
import com.bhm.sdk.support.observer.DownloadEngine
import com.bhm.sdk.support.observer.DownloadObserver

/**
 * @author Buhuiming
 * @description: 下载管理
 * @date :2022/10/19 15:19
 */
class DownloadRequest(private val context: Application) : IRequest {

    override fun newRequest(config: DownloadConfig) {
        DownloadManager.getInstance(context)?.newCall(config)
    }

    override fun startDownload(url: String, fileName: String): Boolean? {
        return DownloadManager.getInstance(context)?.startDownload(url, fileName)
    }

    override fun reStartDownload(url: String, fileName: String): Boolean? {
        return DownloadManager.getInstance(context)?.reStartDownload(url, fileName)
    }

    override fun pauseDownload(url: String, fileName: String): Boolean? {
        return DownloadManager.getInstance(context)?.pauseDownload(url, fileName)
    }

    override fun deleteDownload(url: String, fileName: String): Boolean? {
        return DownloadManager.getInstance(context)?.deleteDownload(url, fileName)
    }

    override fun updateNotification(notification: Notification?) {
        DownloadManager.getInstance(context)?.updateNotification(notification)
    }

    override fun registerCallback(fileName: String, observer: DownloadObserver?) {
        DownloadEngine.get().register(fileName, observer)
    }

    override fun unRegisterCallback(fileName: String) {
        DownloadEngine.get().unRegister(fileName)
    }

    override fun close() {
        DownloadEngine.get().close()
    }
}