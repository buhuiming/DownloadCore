package com.bhm.downloadcore

import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.bhm.sdk.support.DownLoadFileModel
import com.bhm.sdk.support.DownLoadStatus
import com.bhm.sdk.support.DownloadConfig
import com.bhm.sdk.support.DownloadRequest
import com.bhm.sdk.support.observer.DownloadObserver
import com.bhm.sdk.support.utils.DownLoadUtil
import com.bhm.support.sdk.common.BaseViewModel
import com.bhm.support.sdk.utils.NotificationUtil
import timber.log.Timber
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

    private var downloadNotification: Notification? = null

    private val fileModelList = ArrayList<FileModel>()

    fun initDownloadManager() {
        NotificationUtil.getInstance(context)?.init(
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            null
        )
        parentPath = context.getExternalFilesDir("downloadFiles")?.absolutePath
        downloadRequest = DownloadRequest(context)
        downloadNotification = if (Constants.DOWNLOAD_IN_THE_BACKGROUND) {
            downloadNotification()
        } else {
            null
        }
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(3)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setLogger(true)
            .setDownloadOverWiFiOnly(Constants.DOWNLOAD_OVER_WIFI_ONLY) //仅WiFi时下载
            .setDownloadInTheBackground(downloadNotification, Constants.NOTIFICATION_ID)//传空，则退出APP，停止下载
            .setDownloadParentPath(parentPath)
            .setDefaultHeader(null)
            .build()
        downloadRequest?.newRequest(downloadConfig)
    }

    private fun downloadNotification() : Notification? {
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
        return NotificationUtil.getInstance(
            context,
        )?.buildNotificationText(
            title = context.getString(R.string.app_name),
            body = "正在下载",
            pendingIntent = pendingIntent,
            channelId = ""
        )?.build()
    }

    fun initDownloadList() {
        Constants.urls.forEach { url ->
            val fileName = generateFileName(fileModelList, url)
            val fileModel = FileModel(
                downLoadUrl = url,
                fileName = fileName,
                status = getStatus(fileName),
                progress = getProgress(fileName)
            )
            fileModelList.add(fileModel)
        }

        downloadList.postValue(fileModelList)

        fileModelList.forEach {
            downloadRequest?.registerCallback(it.fileName, object : DownloadObserver(context) {
                override fun onInitialize(dLFModel: DownLoadFileModel) {
                    super.onInitialize(dLFModel)
                    Timber.d("onInitialize: " + dLFModel.downLoadUrl)
                    it.status = dLFModel.status
                    it.progress = 0f
                    downloadList.postValue(fileModelList)
                }

                override fun onWaiting(dLFModel: DownLoadFileModel) {
                    super.onWaiting(dLFModel)
                    Timber.d("onWaiting: " + dLFModel.downLoadUrl)
                    it.status = dLFModel.status
                    downloadList.postValue(fileModelList)
                }

                override fun onStop(dLFModel: DownLoadFileModel) {
                    super.onStop(dLFModel)
                    Timber.d("onStop")
                    it.status = dLFModel.status
                    downloadList.postValue(fileModelList)
                }

                override fun onComplete(dLFModel: DownLoadFileModel) {
                    super.onComplete(dLFModel)
                    Timber.d("onComplete")
                    it.status = dLFModel.status
                    downloadList.postValue(fileModelList)
                }

                override fun onProgress(dLFModel: DownLoadFileModel) {
                    super.onProgress(dLFModel)
                    Timber.d("url: " + dLFModel.downLoadUrl)
                    Timber.d(
                        "totalLength: " + dLFModel.totalLength.toString() + ", totalReadBytes: " +
                                dLFModel.downLoadLength.toString() + ", progress: " + dLFModel.progress
                    )
                    it.progress = dLFModel.progress
                    it.status = dLFModel.status
                    downloadList.postValue(fileModelList)
                }

                override fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable) {
                    super.onFail(dLFModel, throwable)
                    Timber.e("onFail----" + throwable.message)
                    it.status = dLFModel.status
                    downloadList.postValue(fileModelList)
                }
            })
        }
    }

    fun onDestroy() {
        downloadRequest?.close()
    }

    private fun getStatus(fileName: String): DownLoadStatus {
        if (DownLoadUtil.checkExistFullFile(context, fileName, parentPath!!)) {
            return DownLoadStatus.COMPETE
        }
        val progress = getProgress(fileName)
        if (progress > 0) {
            return DownLoadStatus.STOP
        }
        return DownLoadStatus.INITIAL
    }

    private fun getProgress(fileName: String): Float {
        return DownLoadUtil.getExistFileProgress(context, fileName, parentPath!!)
    }

    fun startDownload(url: String, fileName: String) {
        downloadRequest?.startDownload(url, fileName)
    }

    fun startAllDownloads() {
        fileModelList.forEach {
            startDownload(it.downLoadUrl, it.fileName)
        }
    }

    fun restartDownload(url: String, fileName: String) {
        downloadRequest?.reStartDownload(url, fileName)
    }

    fun pauseDownload(url: String, fileName: String) {
        downloadRequest?.pauseDownload(url, fileName)
    }

    fun pauseAllDownloads() {
        fileModelList.forEach {
            pauseDownload(it.downLoadUrl, it.fileName)
        }
    }

    fun deleteDownload(url: String, fileName: String) {
        //删除已下载文件
        downloadRequest?.deleteDownload(url, fileName)
    }

    fun deleteAllDownloads() {
        //全部删除已下载文件
        fileModelList.forEach {
            deleteDownload(it.downLoadUrl, it.fileName)
        }
    }

    fun openFile(fileName: String) {
        if (fileName == "") return
        val filePath = parentPath + File.separator + fileName
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
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

    /*重复文件命名*/
    private fun generateFileName(fileModelList: ArrayList<FileModel>, url: String): String {
        if (fileModelList.isEmpty()) {
            return DownLoadUtil.generateFileName(url)
        }
        val newFileModelList = ArrayList<FileModel>()
        fileModelList.forEach {
            if (url == it.downLoadUrl) {
                newFileModelList.add(it)
            }
        }
        if (newFileModelList.isEmpty()) {
            return DownLoadUtil.generateFileName(url)
        }
        return DownLoadUtil.generateFileName(url) + "(${newFileModelList.size})"
    }

}