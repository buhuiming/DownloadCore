package com.bhm.sdk.support

import android.app.Application

/**
 * @author Buhuiming
 * @description: 下载管理
 * @date :2022/10/19 15:19
 */
class DownloadRequest(private val context: Application) : IRequest {

    override fun newRequest(config: DownloadConfig) {
        DownloadManager.getInstance(context)?.newCall(config)
    }

    override fun startDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean? {
        val call = DownloadCallBack()
        callBack?.let {
            call.apply(it)
        }
        return DownloadManager.getInstance(context)?.startDownload(url, call)
    }

    override fun reStartDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean? {
        val call = DownloadCallBack()
        callBack?.let {
            call.apply(it)
        }
        return DownloadManager.getInstance(context)?.reStartDownload(url, call)
    }

    override fun pauseDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean? {
        val call = DownloadCallBack()
        callBack?.let {
            call.apply(it)
        }
        return DownloadManager.getInstance(context)?.pauseDownload(url, call)
    }

    override fun removeDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean? {
        val call = DownloadCallBack()
        callBack?.let {
            call.apply(it)
        }
        return DownloadManager.getInstance(context)?.removeDownload(url, call)
    }
}