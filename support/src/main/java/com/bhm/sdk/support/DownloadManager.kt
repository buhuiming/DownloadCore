package com.bhm.sdk.support

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.os.Bundle
import android.util.Log
import com.bhm.sdk.support.DownloadConfig.Companion.SP_FILE_NAME
import com.bhm.sdk.support.observer.DownloadEngine
import com.bhm.sdk.support.service.DownloadService
import com.bhm.sdk.support.utils.DownLoadUtil
import com.bhm.sdk.support.utils.NetUtil
import com.bhm.sdk.support.utils.SPUtil
import okhttp3.*
import okhttp3.internal.http2.StreamResetException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * @author Buhuiming
 * @description: 下载管理
 * @date :2022/10/19 15:49
 */
internal class DownloadManager private constructor(private val context: Application) {

    private val downloadCallHashMap: ConcurrentHashMap<Call, DownLoadFileModel> = ConcurrentHashMap()

    private var okHttpClient: OkHttpClient? = null

    private var downloadConfig: DownloadConfig? = null

    companion object {
        private var instance: DownloadManager? = null

        private val TAG = DownloadManager::class.simpleName

        fun getInstance(context: Application): DownloadManager? {
            if (instance == null) {
                synchronized(DownloadManager::class.java) {
                    if (instance == null) {
                        instance = DownloadManager(context)
                    }
                }
            }
            return instance
        }
    }

    fun newCall(config: DownloadConfig) {
        downloadConfig = config
        okHttpClient = OkHttpClient.Builder()
            .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
            .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
            .addInterceptor(HeaderInterceptor().make(config))
            .build()
        okHttpClient?.dispatcher?.maxRequestsPerHost = config.getMaxDownloadSize() //每个主机最大请求数为
        okHttpClient?.dispatcher?.maxRequests = config.getMaxDownloadSize() //最大并发请求数为
        if (config.downloadNotification() == null) {
            context.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
                override fun onActivityStarted(activity: Activity) {}
                override fun onActivityResumed(activity: Activity) {}
                override fun onActivityPaused(activity: Activity) {}
                override fun onActivityStopped(activity: Activity) {}
                override fun onActivityDestroyed(activity: Activity) {
                    if (activity.isTaskRoot) {
                        for ((key, _) in downloadCallHashMap) {
                            key.cancel()
                        }
                        printELog("root activity finish, cancel all downloads")
                    }
                    printELog("activity finish, onActivityDestroyed")
                }
            })
        }
    }

    fun startDownload(url: String, fileName: String): Boolean {
        val fileModel = buildModel(url, fileName)
        return startDownload(fileModel)
    }

    fun reStartDownload(url: String, fileName: String): Boolean {
        deleteFile(url, fileName, true)
        val fileModel = buildModel(url, fileName)
        return startDownload(fileModel)
    }

    private fun startDownload(fileModel: DownLoadFileModel): Boolean {
        if (DownLoadUtil.checkExistFullFile(
                context,
                fileModel.fileName,
                fileModel.localParentPath
            )
        ) {
            DownloadEngine.get().onComplete(fileModel)
            printILog("file is already download")
            return false
        }
        for ((_, value) in downloadCallHashMap) {
            if (value.downLoadUrl == fileModel.downLoadUrl && value.fileName == fileModel.fileName) {
                //已经添加下载了
                printILog("file is already add to download")
                return false
            }
        }

        if (!NetUtil.isNetWorkConnected(context)) {
            printILog("NetWork unConnected")
            return false
        }

        if (downloadConfig?.isDownloadOverWiFiOnly() == true && !NetUtil.isWifiConnected(context)) {
            printILog("download over WiFi only")
            return false
        }

        printILog("queuedCallsCount: " + okHttpClient!!.dispatcher.queuedCallsCount())
        printILog("runningCallsCount: " + okHttpClient!!.dispatcher.runningCallsCount())
        if (okHttpClient!!.dispatcher.queuedCallsCount() + okHttpClient!!.dispatcher
                .runningCallsCount() >= (downloadConfig?.getMaxDownloadSize()?: DownloadConfig.MAX_DOWNING_SIZE)
        ) {
            //等待队列数和下载队列数超过1个，则加入等待
            DownloadEngine.get().onWaiting(fileModel)
        }

        if (downloadConfig?.downloadNotification() != null && downloadCallHashMap.size == 0) {
            updateNotification(downloadConfig?.downloadNotification())
            printILog("启动下载服务 ")
        }

        val request: Request = Request.Builder()
            .url(fileModel.downLoadUrl)
            .header("range", "bytes=" + fileModel.downLoadLength.toString() + "-")
            .build()

        // 使用OkHttp请求服务器
        val call = okHttpClient!!.newCall(request)
        downloadCallHashMap[call] = fileModel
        printILog("add a download")
//        call.execute(); 这个数同步操作
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                printELog("onFailure: " + (e.message?: " IOException") + ", " + fileModel.downLoadUrl)
                if (e is StreamResetException || e is SocketException || call.isCanceled()) {
                    if (DownLoadUtil.getExistFileProgress(
                            context,
                            fileModel.fileName,
                            downloadConfig?.getDownloadParentPath()!!,
                        ) == 0f) {
                        //用户删除队列中的请求，显示未开始
                        DownloadEngine.get().onInitialize(fileModel)
                    } else {
                        DownloadEngine.get().onStop(fileModel)
                    }
                } else {
                    DownloadEngine.get().onFail(fileModel, e)
                }
                removeDownload(fileModel.downLoadUrl, fileModel.fileName)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body
                //保存文件的地址和大小，这个大小最好后台直接返回
                if (response.isSuccessful) {
                    if (body != null) {
                        if (fileModel.downLoadLength == 0L && body.contentLength() > 0) {
                            SPUtil.put(
                                context,
                                SP_FILE_NAME,
                                fileModel.fileName,
                                body.contentLength()
                            )
                        }
                        saveFile(fileModel, body.byteStream(), body.contentLength())
                    } else {
                        printELog("ResponseBody is null.")
                        DownloadEngine.get().onFail(fileModel, Exception("ResponseBody is null."))
                    }
                } else {
                    printELog("isUnSuccessful")
                    DownloadEngine.get().onFail(fileModel, Exception("{\"code\":${response.code},\"message\":\"${response.message}\"}"))
                }
            }
        })
        return false
    }

    private fun deleteFile(url: String, fileName: String, remove: Boolean) {
        val parentPath: String = downloadConfig?.getDownloadParentPath()?: ""
        val file = File(parentPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        val downLoadFile = File(file, fileName)
        DownLoadUtil.clearDir(downLoadFile, true)
        SPUtil.removeKeyValue(
            context,
            SP_FILE_NAME,
            fileName
        )
        if (remove) {
            removeDownload(url, fileName)
        }
    }

    fun pauseDownload(url: String, fileName: String): Boolean{
        for ((key, value) in downloadCallHashMap) {
            if (value.downLoadUrl == url && value.fileName == fileName) {
                key.cancel()
                DownloadEngine.get().onStop(value)
                printILog("cancel download")
                return true
            }
        }
        printILog("cancel download fail：")
        return false
    }

    @Synchronized
    fun deleteDownload(url: String, fileName: String): Boolean {
        deleteFile(url, fileName, false)
        val buildModel = buildModel(url, fileName)
        if (downloadCallHashMap.size == 0) {
            DownloadEngine.get().onInitialize(buildModel)
            return true
        }
        val iterator = downloadCallHashMap.iterator()
        while (iterator.hasNext()) {
            val model = iterator.next()
            if (model.value.downLoadUrl == url && model.value.fileName == fileName) {
                model.key.cancel()
                DownloadEngine.get().onInitialize(model.value)
                iterator.remove()
                printILog("delete download url")
                isAllComplete()
                return true
            } else {
                DownloadEngine.get().onInitialize(buildModel)
            }
        }
        printILog("download already delete")
        return false
    }

    @Synchronized
    private fun removeDownload(url: String, fileName: String): Boolean {
        printILog("remove download url downloadCallHashMap：" + downloadCallHashMap.size)
        val iterator = downloadCallHashMap.iterator()
        while (iterator.hasNext()) {
            val model = iterator.next()
            if (model.value.downLoadUrl == url && model.value.fileName == fileName) {
                iterator.remove()
                printILog("remove download url downloadCallHashMap2：" + downloadCallHashMap.size)
                isAllComplete()
                return true
            }
        }
        printILog("remove download fail")
        return false
    }

    private fun isAllComplete() {
        if (downloadCallHashMap.size == 0 && downloadConfig?.downloadNotification() != null) {
            printILog("关闭下载服务 ")
            DownloadService.destroy(context)
        }
    }

    private fun buildModel(url: String, fileName: String): DownLoadFileModel {
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
            progress = DownLoadUtil.getExistFileProgress(context, fileName, parentPath)
        )
    }

    private fun saveFile(dLFModel: DownLoadFileModel, inputString: InputStream, byteLength: Long) {
        printILog("saveFile--> byteLength: $byteLength")
        if (byteLength == 0L && dLFModel.downLoadLength > 0) {
            DownloadEngine.get().onComplete(dLFModel)
            return  //已经下载好了
        }
        val totalLength: Long = dLFModel.downLoadLength + byteLength
        var lastProgress = 0f
        dLFModel.totalLength = totalLength
        try {
            val file = File(dLFModel.localParentPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val fos = FileOutputStream(dLFModel.downLoadFile, true)
            val b = ByteArray(2048)
            var len: Int
            while (inputString.read(b).also { len = it } != -1) {
                fos.write(b, 0, len)
                dLFModel.downLoadLength = dLFModel.downLoadFile?.length()?: 0
                val progress: Float = dLFModel.downLoadLength * 100f / totalLength
                val finalProgress: Float = String.format("%.1f", progress).toFloat()
                dLFModel.progress = finalProgress
                if (dLFModel.progress >= 100) {
                    removeDownload(dLFModel.downLoadUrl, dLFModel.fileName)
                }
                if (lastProgress < dLFModel.progress) {
                    lastProgress = dLFModel.progress
                    DownloadEngine.get().onProgress(dLFModel)
                }
            }
            fos.flush()
            inputString.close()
            fos.close()
        } catch (e: java.lang.Exception) {
            if (e is StreamResetException || e is SocketException) {
                printELog("saving cancel by user" + ", " + dLFModel.downLoadUrl)
                if (DownLoadUtil.getExistFileProgress(
                        context,
                        dLFModel.fileName,
                        downloadConfig?.getDownloadParentPath()!!
                    ) == 0f) {
                    //用户删除队列中的请求，显示未开始
                    DownloadEngine.get().onInitialize(dLFModel)
                } else {
                    DownloadEngine.get().onStop(dLFModel)
                }
            } else {
                printELog("saving onFailure: " + (e.message?: " IOException") + ", " + dLFModel.downLoadUrl)
                DownloadEngine.get().onFail(dLFModel, e)
            }
            removeDownload(dLFModel.downLoadUrl, dLFModel.fileName)
        } finally {
            if (dLFModel.downLoadLength >= totalLength) {
                DownloadEngine.get().onComplete(dLFModel)
            }
        }
    }

    fun updateNotification(notification: Notification?) {
        if (downloadConfig?.downloadNotification() != null) {
            DownloadService.start(context, notification, downloadConfig?.downloadNotificationId())
        }
    }

    private fun printILog(message: String) {
        if (downloadConfig?.isLogger() == true) {
            Log.i(TAG, message)
        }
    }

    private fun printELog(message: String) {
        Log.e(TAG, message)
    }
}