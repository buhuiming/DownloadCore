package com.bhm.sdk.support

import android.app.Application
import android.util.Log
import com.bhm.sdk.support.DownloadConfig.Companion.SP_FILE_NAME
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
            .writeTimeout(config.getWriteTimeout().toLong(), TimeUnit.SECONDS)
            .readTimeout(config.getReadTimeout().toLong(), TimeUnit.SECONDS)
            .connectTimeout(config.getConnectTimeout().toLong(), TimeUnit.SECONDS)
            .build()
        okHttpClient?.dispatcher?.maxRequestsPerHost = config.getMaxDownloadSize() //每个主机最大请求数为
        okHttpClient?.dispatcher?.maxRequests = config.getMaxDownloadSize() //最大并发请求数为
    }


    fun startDownload(url: String, callBack: IDownLoadCallBack): Boolean {
        val fileModel = buildModel(url)
        return startDownload(fileModel, callBack)
    }

    fun reStartDownload(url: String, callBack: IDownLoadCallBack): Boolean {
        deleteFile(url)
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
            Log.i(DownloadManager::class.simpleName, "file is already download")
            return false
        }
        downloadCallHashMap.forEach {
            if (it.value.downLoadUrl == fileModel.downLoadUrl) {
                //已经添加下载了
                Log.i(DownloadManager::class.simpleName, "file is already add to download")
                return false
            }
        }
        Log.i(DownloadManager::class.simpleName, "queuedCallsCount: " + okHttpClient!!.dispatcher.queuedCallsCount())
        Log.i(DownloadManager::class.simpleName, "runningCallsCount: " + okHttpClient!!.dispatcher.runningCallsCount())
        if (okHttpClient!!.dispatcher.queuedCallsCount() + okHttpClient!!.dispatcher
                .runningCallsCount() >= (downloadConfig?.getMaxDownloadSize()?: DownloadConfig.MAX_DOWNING_SIZE)
        ) {
            //等待队列数和下载队列数超过1个，则加入等待
            callBack.onWaiting(fileModel)
        }

        val request: Request = Request.Builder()
            .url(fileModel.downLoadUrl)
            .header("range", "bytes=" + fileModel.downLoadLength.toString() + "-")
            .build()

        // 使用OkHttp请求服务器
        val call = okHttpClient!!.newCall(request)
        downloadCallHashMap[call] = fileModel
        Log.i(DownloadManager::class.simpleName, "add a download")
//        call.execute(); 这个数同步操作
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(DownloadManager::class.simpleName, e.message?: "onFailure IOException")
                callBack.onFail(fileModel, e)
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
                    Log.e(DownloadManager::class.simpleName, "ResponseBody is null.")
                    callBack.onFail(fileModel, Exception("ResponseBody is null."))
                }
            }
        })
        return false
    }

    private fun deleteFile(url: String) {
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
        removeDownload(url)
    }

    fun pauseDownload(url: String, callBack: IDownLoadCallBack): Boolean{
        downloadCallHashMap.forEach {
            if (it.value.downLoadUrl == url) {
                val call: Call = it.key
                call.cancel()
                callBack.onStop(it.value)
                Log.i(DownloadManager::class.simpleName, "cancel download")
                return true
            }
        }
        Log.i(DownloadManager::class.simpleName, "cancel download fail")
        return false
    }

    @Synchronized
    fun removeDownload(url: String, callBack: IDownLoadCallBack): Boolean {
        val iterator = downloadCallHashMap.iterator()
        while (iterator.hasNext()) {
            val model = iterator.next()
            if (model.value.downLoadUrl == url) {
                iterator.remove()
                deleteFile(url)
                callBack.onStop(model.value)
                Log.i(DownloadManager::class.simpleName, "remove download url")
                return true
            }
        }
        Log.i(DownloadManager::class.simpleName, "remove download fail")
        return false
    }

    @Synchronized
    private fun removeDownload(url: String): Boolean {
        Log.i(DownloadManager::class.simpleName, "remove download url downloadCallHashMap：" + downloadCallHashMap.size)
        val iterator = downloadCallHashMap.iterator()
        while (iterator.hasNext()) {
            val model = iterator.next()
            if (model.value.downLoadUrl == url) {
                iterator.remove()
                Log.i(DownloadManager::class.simpleName, "remove download url downloadCallHashMap2：" + downloadCallHashMap.size)
            }
        }
        Log.i(DownloadManager::class.simpleName, "remove download fail")
        return false
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
        Log.i(DownloadManager::class.simpleName, "saveFile--> byteLength: $byteLength")
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
                callBack.saveFile(dLFModel)
            }
            fos.flush()
            inputString.close()
            fos.close()
        } catch (e: java.lang.Exception) {
            if (e is StreamResetException || e is SocketException) {
                Log.e(DownloadManager::class.simpleName, "cancel by user")
                callBack.onStop(dLFModel)
                removeDownload(dLFModel.downLoadUrl)
            } else {
                Log.e(DownloadManager::class.simpleName, e.message?: "Exception")
                callBack.onFail(dLFModel, e)
                removeDownload(dLFModel.downLoadUrl)
            }
        } finally {
            if (dLFModel.downLoadLength >= totalLength) {
                callBack.onComplete(dLFModel)
            }
        }
    }
}