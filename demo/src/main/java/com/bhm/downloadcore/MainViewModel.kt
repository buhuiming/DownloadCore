package com.bhm.downloadcore

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.bhm.sdk.support.*
import com.bhm.sdk.support.interfaces.DownloadCallBack
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

    private val callBackList = HashMap<String, HashMap<String, (DownloadCallBack.() -> Unit)?>>()

    private var parentPath: String? = null

    private var downloadNotification: Notification? = null

    private val downloadInTheBackground = true

    private val fileModelList = ArrayList<FileModel>()

    fun initDownloadManager() {
        NotificationUtil.getInstance(context)?.init(
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            null
        )
        parentPath = context.getExternalFilesDir("downloadFiles")?.absolutePath
        downloadRequest = DownloadRequest(context)
        downloadNotification = if (downloadInTheBackground) {
            downloadNotification()
        } else {
            null
        }
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(3)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setDownloadOverWiFiOnly(Constants.DOWNLOAD_OVER_WIFI_ONLY) //仅WiFi时下载
            .setDownloadInTheBackground(downloadNotification, Constants.NOTIFICATION_ID)
//            .setDownloadInTheBackground(null)//传空，则退出APP，停止下载
            .setDownloadParentPath(parentPath)
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
            val map = HashMap<String, (DownloadCallBack.() -> Unit)?>()
            map[it.downLoadUrl] = {
                onInitialize { model->
                    Timber.d("onInitialize: " + model.downLoadUrl)
                    it.status = model.status
                    it.progress = 0f
                    downloadList.postValue(fileModelList)
                }
                onWaiting { model->
                    Timber.d("onWaiting: " + model.downLoadUrl)
                    it.status = model.status
                    downloadList.postValue(fileModelList)
                }
                onProgress { model->
                    Timber.d("url: " + model.downLoadUrl)
                    Timber.d(
                        "totalLength: " + model.totalLength.toString() + ", totalReadBytes: " +
                                model.downLoadLength.toString() + ", progress: " + model.progress
                    )
                    it.progress = model.progress
                    it.status = model.status
                    downloadList.postValue(fileModelList)
                }
                onStop { model->
                    Timber.d("onStop")
                    it.status = model.status
                    downloadList.postValue(fileModelList)
                }
                onComplete { model->
                    Timber.d("onComplete")
                    it.status = model.status
                    downloadList.postValue(fileModelList)
                }
                onFail { model, throwable ->
                    Timber.d("onFail" + throwable.message)
                    it.status = model.status
                    downloadList.postValue(fileModelList)
                }
            }
            callBackList[it.fileName] = map
        }
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
        downloadRequest?.startDownload(url, fileName, callBackList[fileName]?.get(url))
    }

    fun startAllDownloads() {
        callBackList.forEach {
            startDownload(it.value.keys.first(), it.key)
        }
    }

    fun restartDownload(url: String, fileName: String) {
        downloadRequest?.reStartDownload(url, fileName, callBackList[fileName]?.get(url))
    }

    fun pauseDownload(url: String, fileName: String) {
        downloadRequest?.pauseDownload(url, fileName, callBackList[fileName]?.get(url))
    }

    fun pauseAllDownloads() {
        callBackList.forEach {
            pauseDownload(it.value.keys.first(), it.key)
        }
    }

    fun deleteDownload(url: String, fileName: String) {
        //删除已下载文件
        downloadRequest?.deleteDownload(url, fileName, callBackList[fileName]?.get(url))
    }

    fun deleteAllDownloads() {
        //全部删除已下载文件
        callBackList.forEach {
            deleteDownload(it.value.keys.first(), it.key)
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