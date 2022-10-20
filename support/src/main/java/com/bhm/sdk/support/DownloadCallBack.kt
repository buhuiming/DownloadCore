package com.bhm.sdk.support

import android.os.Handler
import android.os.Looper

/**
 * @author Buhuiming
 * @description: 下载回调
 * @date :2022/10/20 13:49
 */
class DownloadCallBack : IDownLoadCallBack{

    private var _waiting: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _stop: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _complete: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _progress: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _save: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _fail: ((dLFModel: DownLoadFileModel, throwable: Throwable) -> Unit)? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private var lastProgress = 0f

    fun onWaiting(value: (dLFModel: DownLoadFileModel) -> Unit) {
        _waiting = value
    }

    fun onStop(value: (dLFModel: DownLoadFileModel) -> Unit) {
        _stop = value
    }
    fun onComplete(value: (dLFModel: DownLoadFileModel) -> Unit) {
        _complete = value
    }
    fun onProgress(value: (dLFModel: DownLoadFileModel) -> Unit) {
        _progress = value
    }
    fun saveFile(value: (dLFModel: DownLoadFileModel) -> Unit) {
        _save = value
    }
    fun onFail(value: (dLFModel: DownLoadFileModel, throwable: Throwable) -> Unit) {
        _fail = value
    }

    override fun onWaiting(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.WAITING
        mainHandler.post { _waiting?.invoke(dLFModel) }
    }

    override fun onStop(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.STOP
        mainHandler.post { _stop?.invoke(dLFModel) }
    }

    override fun onComplete(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.COMPETE
        mainHandler.post { _complete?.invoke(dLFModel) }
    }

    override fun onProgress(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.DOWNING
        mainHandler.post { _progress?.invoke(dLFModel) }
    }

    override fun saveFile(dLFModel: DownLoadFileModel) {
        if (lastProgress < dLFModel.progress) {
            lastProgress = dLFModel.progress
            onProgress(dLFModel)
        }
        mainHandler.post { _save?.invoke(dLFModel) }
    }

    override fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable) {
        dLFModel.status = DownLoadStatus.FAIL
        mainHandler.post { _fail?.invoke(dLFModel, throwable) }
    }
}