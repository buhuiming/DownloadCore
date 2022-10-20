package com.bhm.sdk.support

import android.app.Application
import java.io.File

/**
 * @author Buhuiming
 * @description: 下载管理
 * @date :2022/10/19 15:19
 */
class DownloadRequest(private val context: Application) : IRequest {

    private var downloadConfig: DownloadConfig? = null

    override fun newRequest(config: DownloadConfig) {
        downloadConfig = config
        DownloadManager.getInstance(context)?.newCall(config)
    }

    override fun startDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean? {
        val call = DownloadCallBack()
        callBack?.let {
            call.apply(it)
        }
        return DownloadManager.getInstance(context)?.startDownload(buildModel(url), call)
    }

    override fun reStartDownload(url: String, callBack: (DownloadCallBack.() -> Unit)?): Boolean? {
        val call = DownloadCallBack()
        callBack?.let {
            call.apply(it)
        }
        return DownloadManager.getInstance(context)?.reStartDownload(buildModel(url), call)
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

    private fun buildModel(url: String): DownLoadFileModel {
        val fileName: String = DownLoadUtil.getMD5FileName(url)
        val parentPath: String = downloadConfig?.getDownloadParentPath()?: ""
        var downLoadLength: Long = 0
        val file = File(parentPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        val downLoadFile = File(file, fileName)
        if (downLoadFile.exists()) {
            downLoadLength = downLoadFile.length()
        }
        return DownLoadFileModel(
            downLoadUrl = url,
            localParentPath = parentPath,
            localPath = downLoadFile.absolutePath,
            fileName = fileName,
            downLoadFile = downLoadFile,
            status = DownLoadStatus.INITIAL,
            downLoadLength = downLoadLength,
            totalLength = 0,
            progress = DownLoadUtil.getExistFileProgress(context, url, parentPath)
        )
    }
}