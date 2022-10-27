package com.bhm.sdk.support.interfaces

import android.app.Notification
import com.bhm.sdk.support.DownloadConfig
import com.bhm.sdk.support.observer.DownloadObserver

/**
 * @author Buhuiming
 * @date :2022/10/19 15:21
 */
internal interface IRequest {
    fun newRequest(config: DownloadConfig)
    fun startDownload(url: String, fileName: String): Boolean?
    fun reStartDownload(url: String, fileName: String): Boolean?
    fun pauseDownload(url: String, fileName: String): Boolean?
    fun deleteDownload(url: String, fileName: String): Boolean?
    fun updateNotification(notification: Notification?)
    fun registerCallback(fileName: String, observer: DownloadObserver?)
    fun unRegisterCallback(fileName: String)
    fun close()
}