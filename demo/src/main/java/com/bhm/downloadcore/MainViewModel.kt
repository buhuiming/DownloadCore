package com.bhm.downloadcore

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.bhm.sdk.support.DownLoadUtil
import com.bhm.sdk.support.DownloadConfig
import com.bhm.sdk.support.DownloadRequest
import com.bhm.support.sdk.common.BaseViewModel
import java.io.File

/**
 * @author Buhuiming
 * @description:
 * @date :2022/10/20 9:28
 */
class MainViewModel(private val context: Application) : BaseViewModel(context = context) {

    private var downloadRequest: DownloadRequest? = null

    val downloadList = MutableLiveData<ArrayList<FileModel>>()

    private var parentPath: String? = null

    fun initDownloadManager() {
        parentPath = context.getExternalFilesDir("downloadFiles")?.absolutePath
        downloadRequest = DownloadRequest()
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(3)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setDownloadParentPath(parentPath)
            .build()
        downloadRequest?.newRequest(downloadConfig)
    }

    fun initDownloadList() {
        val list = arrayListOf(
            FileModel(downLoadUrl = Constants.urls[0], fileName = DownLoadUtil.getMD5FileName(Constants.urls[0])),
            FileModel(downLoadUrl = Constants.urls[1], fileName = DownLoadUtil.getMD5FileName(Constants.urls[1])),
            FileModel(downLoadUrl = Constants.urls[2], fileName = DownLoadUtil.getMD5FileName(Constants.urls[2])),
            FileModel(downLoadUrl = Constants.urls[3], fileName = DownLoadUtil.getMD5FileName(Constants.urls[3])),
            FileModel(downLoadUrl = Constants.urls[4], fileName = DownLoadUtil.getMD5FileName(Constants.urls[4])),
        )
        downloadList.postValue(list)
    }

    fun startDownload(url: String?) {
        downloadRequest?.startDownload(url)
    }

    fun startAllDownloads() {
        downloadRequest?.startAllDownloads()
    }

    fun restartDownload(url: String?) {
        //删除已下载文件
        downloadRequest?.startDownload(url)
    }

    fun pauseDownload(url: String?) {
        downloadRequest?.pauseDownload(url)
    }

    fun pauseAllDownloads() {
        downloadRequest?.pauseAllDownloads()
    }

    fun removeDownload(url: String?) {
        //删除已下载文件
        downloadRequest?.removeDownload(url)
    }

    fun removeAllDownloads() {
        //全部删除已下载文件
        downloadRequest?.removeAllDownloads()
    }

    fun openFile(fileName: String?) {
        if (fileName == null || fileName == "") return
        val filePath = parentPath + File.separator + fileName
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            val contentUri = FileProvider.getUriForFile(
                context,
                context.packageName.toString() + ".fileprovider",
                File(filePath)
            )
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            val uri = Uri.fromFile(File(filePath))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}