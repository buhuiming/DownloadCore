package com.bhm.sdk.support

import com.bhm.sdk.support.DownloadConfig

/**
 * @author Buhuiming
 * @date :2022/10/19 15:21
 */
interface IRequest {
    fun newRequest(config: DownloadConfig?)
    fun startDownload(url: String?): Boolean
    fun startAllDownloads(): Boolean
    fun pauseDownload(url: String?): Boolean
    fun pauseAllDownloads(): Boolean
    fun removeDownload(url: String?): Boolean
    fun removeAllDownloads(): Boolean
}