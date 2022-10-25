package com.bhm.sdk.support

import android.app.Activity
import android.app.Application
import android.app.Notification
import android.os.Bundle
import android.util.Log
import com.bhm.sdk.support.DownloadConfig.Companion.SP_FILE_NAME
import com.bhm.sdk.support.interfaces.IDownLoadCallBack
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
                        Log.e(TAG, "root activity finish, cancel all downloads")
                    }
                    Log.e(TAG, "activity finish, onActivityDestroyed")
                }
            })
        }
    }

    fun startDownload(url: String, callBack: IDownLoadCallBack): Boolean {
        val fileModel = buildModel(url)
        return startDownload(fileModel, callBack)
    }

    fun reStartDownload(url: String, callBack: IDownLoadCallBack): Boolean {
        deleteFile(url, true)
        val fileModel = buildModel(url)
        return startDownload(fileModel, callBack)
    }

    private fun startDownload(fileModel: DownLoadFileModel, callBack: IDownLoadCallBack): Boolean {
        if (DownLoadUtil.checkExistFullFile(context,
                fileModel.downLoadUrl,
                fileModel.localParentPath
            )
        ) {
            callBack.onComplete(fileModel)
            Log.i(TAG, "file is already download")
            return false
        }
        for ((_, value) in downloadCallHashMap) {
            if (value.downLoadUrl == fileModel.downLoadUrl) {
                //已经添加下载了
                Log.i(TAG, "file is already add to download")
                return false
            }
        }

        if (!NetUtil.isNetWorkConnected(context)) {
            Log.i(TAG, "NetWork unConnected")
            return false
        }

        if (downloadConfig?.downloadOverWiFiOnly() == true && !NetUtil.isWifiConnected(context)) {
            Log.i(TAG, "download over WiFi only")
            return false
        }

        Log.i(TAG, "queuedCallsCount: " + okHttpClient!!.dispatcher.queuedCallsCount())
        Log.i(TAG, "runningCallsCount: " + okHttpClient!!.dispatcher.runningCallsCount())
        if (okHttpClient!!.dispatcher.queuedCallsCount() + okHttpClient!!.dispatcher
                .runningCallsCount() >= (downloadConfig?.getMaxDownloadSize()?: DownloadConfig.MAX_DOWNING_SIZE)
        ) {
            //等待队列数和下载队列数超过1个，则加入等待
            callBack.onWaiting(fileModel)
        }

        if (downloadConfig?.downloadNotification() != null && downloadCallHashMap.size == 0) {
            updateNotification(downloadConfig?.downloadNotification())
            Log.i(TAG, "启动下载服务 ")
        }

        val request: Request = Request.Builder()
            .url(fileModel.downLoadUrl)
            .header("range", "bytes=" + fileModel.downLoadLength.toString() + "-")
            .build()

        // 使用OkHttp请求服务器
        val call = okHttpClient!!.newCall(request)
        downloadCallHashMap[call] = fileModel
        Log.i(TAG, "add a download")
//        call.execute(); 这个数同步操作
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: " + (e.message?: " IOException") + ", " + fileModel.downLoadUrl)
                if (e is StreamResetException || e is SocketException || call.isCanceled()) {
                    if (DownLoadUtil.getExistFileProgress(context, fileModel.downLoadUrl,
                            downloadConfig?.getDownloadParentPath()!!) == 0f) {
                        //用户删除队列中的请求，显示未开始
                        callBack.onInitialize(fileModel)
                    } else {
                        callBack.onStop(fileModel)
                    }
                } else {
                    callBack.onFail(fileModel, e)
                }
                removeDownload(fileModel.downLoadUrl)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body
                //保存文件的地址和大小，这个大小最好后台直接返回
                if (body != null) {
                    if (fileModel.downLoadLength == 0L && body.contentLength() > 0) {
                        SPUtil.put(
                            context,
                            SP_FILE_NAME,
                            fileModel.downLoadUrl,
                            body.contentLength()
                        )
                    }
                    saveFile(fileModel, body.byteStream(), body.contentLength(), callBack)
                } else {
                    Log.e(TAG, "ResponseBody is null.")
                    callBack.onFail(fileModel, Exception("ResponseBody is null."))
                }
            }
        })
        return false
    }

    private fun deleteFile(url: String, remove: Boolean) {
        val fileName: String = DownLoadUtil.getMD5FileName(url)
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
            url
        )
        if (remove) {
            removeDownload(url)
        }
    }

    fun pauseDownload(url: String, callBack: IDownLoadCallBack): Boolean{
        for ((key, value) in downloadCallHashMap) {
            if (value.downLoadUrl == url) {
                key.cancel()
                callBack.onStop(value)
                Log.i(TAG, "cancel download")
                return true
            }
        }
        Log.i(TAG, "cancel download fail：")
        return false
    }

    @Synchronized
    fun deleteDownload(url: String, callBack: IDownLoadCallBack): Boolean {
        deleteFile(url, false)
        val buildModel = buildModel(url)
        if (downloadCallHashMap.size == 0) {
            callBack.onInitialize(buildModel)
            return true
        }
        val iterator = downloadCallHashMap.iterator()
        while (iterator.hasNext()) {
            val model = iterator.next()
            if (model.value.downLoadUrl == url) {
                model.key.cancel()
                callBack.onInitialize(model.value)
                iterator.remove()
                Log.i(TAG, "delete download url")
                isAllComplete()
                return true
            } else {
                callBack.onInitialize(buildModel)
            }
        }
        Log.i(TAG, "download already delete")
        return false
    }

    @Synchronized
    private fun removeDownload(url: String): Boolean {
        Log.i(TAG, "remove download url downloadCallHashMap：" + downloadCallHashMap.size)
        val iterator = downloadCallHashMap.iterator()
        while (iterator.hasNext()) {
            val model = iterator.next()
            if (model.value.downLoadUrl == url) {
                iterator.remove()
                Log.i(TAG, "remove download url downloadCallHashMap2：" + downloadCallHashMap.size)
                isAllComplete()
                return true
            }
        }
        Log.i(TAG, "remove download fail")
        return false
    }

    private fun isAllComplete() {
        if (downloadCallHashMap.size == 0 && downloadConfig?.downloadNotification() != null) {
            Log.i(TAG, "关闭下载服务 ")
            DownloadService.destroy(context)
        }
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

    private fun saveFile(dLFModel: DownLoadFileModel, inputString: InputStream, byteLength: Long, callBack: IDownLoadCallBack) {
        Log.i(TAG, "saveFile--> byteLength: $byteLength")
        if (byteLength == 0L && dLFModel.downLoadLength > 0) {
            callBack.onComplete(dLFModel)
            return  //已经下载好了
        }
        val totalLength: Long = dLFModel.downLoadLength + byteLength
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
                    removeDownload(dLFModel.downLoadUrl)
                }
                callBack.onProgress(dLFModel)
            }
            fos.flush()
            inputString.close()
            fos.close()
        } catch (e: java.lang.Exception) {
            if (e is StreamResetException || e is SocketException) {
                Log.e(TAG, "saving cancel by user" + ", " + dLFModel.downLoadUrl)
                if (DownLoadUtil.getExistFileProgress(context, dLFModel.downLoadUrl,
                        downloadConfig?.getDownloadParentPath()!!) == 0f) {
                    //用户删除队列中的请求，显示未开始
                    callBack.onInitialize(dLFModel)
                } else {
                    callBack.onStop(dLFModel)
                }
            } else {
                Log.e(TAG, "saving onFailure: " + (e.message?: " IOException") + ", " + dLFModel.downLoadUrl)
                callBack.onFail(dLFModel, e)
            }
            removeDownload(dLFModel.downLoadUrl)
        } finally {
            if (dLFModel.downLoadLength >= totalLength) {
                callBack.onComplete(dLFModel)
            }
        }
    }

    fun updateNotification(notification: Notification?) {
        if (downloadConfig?.downloadNotification() != null) {
            DownloadService.start(context, notification, downloadConfig?.downloadNotificationId())
        }
    }
}