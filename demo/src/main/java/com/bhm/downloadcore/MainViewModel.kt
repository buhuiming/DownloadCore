package com.bhm.downloadcore

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.bhm.sdk.support.*
import com.bhm.support.sdk.common.BaseViewModel
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

    private val callBackList = HashMap<String, (DownloadCallBack.() -> Unit)?>()

    private var parentPath: String? = null

    fun initDownloadManager() {
        parentPath = context.getExternalFilesDir("downloadFiles")?.absolutePath
        downloadRequest = DownloadRequest(context)
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(2)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setDownloadParentPath(parentPath)
            .build()
        downloadRequest?.newRequest(downloadConfig)
    }

    fun initDownloadList() {
        val list = arrayListOf(
            FileModel(
                downLoadUrl = Constants.urls[0],
                fileName = DownLoadUtil.getMD5FileName(Constants.urls[0]),
                status = getStatus(Constants.urls[0]),
                progress = getProgress(Constants.urls[0])
            ),
            FileModel(
                downLoadUrl = Constants.urls[1],
                fileName = DownLoadUtil.getMD5FileName(Constants.urls[1]),
                status = getStatus(Constants.urls[1]),
                progress = getProgress(Constants.urls[1])
            ),
            FileModel(
                downLoadUrl = Constants.urls[2],
                fileName = DownLoadUtil.getMD5FileName(Constants.urls[2]),
                status = getStatus(Constants.urls[2]),
                progress = getProgress(Constants.urls[2])
            ),
            FileModel(
                downLoadUrl = Constants.urls[3],
                fileName = DownLoadUtil.getMD5FileName(Constants.urls[3]),
                status = getStatus(Constants.urls[3]),
                progress = getProgress(Constants.urls[3])
            ),
            FileModel(
                downLoadUrl = Constants.urls[4],
                fileName = DownLoadUtil.getMD5FileName(Constants.urls[4]),
                status = getStatus(Constants.urls[4]),
                progress = getProgress(Constants.urls[4])
            ),
        )
        downloadList.postValue(list)

        list.forEach {
            callBackList[it.downLoadUrl] = {
                onInitialize { model->
                    Timber.d("onInitialize: " + model.downLoadUrl)
                    it.status = model.status
                    downloadList.postValue(list)
                }
                onWaiting { model->
                    Timber.d("onWaiting: " + model.downLoadUrl)
                    it.status = model.status
                    downloadList.postValue(list)
                }
                onProgress { model->
                    Timber.d("url: " + model.downLoadUrl)
                    Timber.d(
                        "totalLength: " + model.totalLength.toString() + ", totalReadBytes: " +
                                model.downLoadLength.toString() + ", progress: " + model.progress
                    )
                    it.progress = model.progress
                    it.status = model.status
                    downloadList.postValue(list)
                }
                onStop { model->
                    Timber.d("onStop")
                    it.status = model.status
                    downloadList.postValue(list)
                }
                onComplete { model->
                    Timber.d("onComplete")
                    it.status = model.status
                    downloadList.postValue(list)
                }
                onFail { model, throwable ->
                    Timber.d("onFail" + throwable.message)
                    it.status = model.status
                    downloadList.postValue(list)
                }
            }
        }
    }

    private fun getStatus(url: String): DownLoadStatus {
        if (DownLoadUtil.checkExistFullFile(context, url, parentPath!!)) {
            return DownLoadStatus.COMPETE
        }
        val progress = getProgress(url)
        if (progress > 0) {
            return DownLoadStatus.STOP
        }
        return DownLoadStatus.INITIAL
    }

    private fun getProgress(url: String): Float {
        return DownLoadUtil.getExistFileProgress(context, url, parentPath!!)
    }

    fun startDownload(url: String) {
        downloadRequest?.startDownload(url, callBackList[url])
    }

    fun startAllDownloads() {
        callBackList.forEach {
            startDownload(it.key)
        }
    }

    fun restartDownload(url: String) {
        downloadRequest?.reStartDownload(url, callBackList[url])
    }

    fun pauseDownload(url: String) {
        downloadRequest?.pauseDownload(url, callBackList[url])
    }

    fun pauseAllDownloads() {
        callBackList.forEach {
            pauseDownload(it.key)
        }
    }

    fun deleteDownload(url: String) {
        //删除已下载文件
        downloadRequest?.deleteDownload(url, callBackList[url])
    }

    fun deleteAllDownloads() {
        //全部删除已下载文件
        callBackList.forEach {
            deleteDownload(it.key)
        }
    }

    fun openFile(fileName: String?) {
        if (fileName == null || fileName == "") return
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
}