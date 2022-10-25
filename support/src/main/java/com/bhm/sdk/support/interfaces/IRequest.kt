package com.bhm.sdk.support.interfaces

import com.bhm.sdk.support.DownloadConfig

/**
 * @author Buhuiming
 * @date :2022/10/19 15:21
 */
interface IRequest {
    fun newRequest(config: DownloadConfig)
    fun startDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean?
    fun reStartDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean?
    fun pauseDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean?
    fun deleteDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean?
}