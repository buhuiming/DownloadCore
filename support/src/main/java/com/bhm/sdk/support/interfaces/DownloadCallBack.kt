package com.bhm.sdk.support.interfaces

import android.content.Context
import com.bhm.sdk.support.DownLoadFileModel
import com.bhm.sdk.support.DownLoadStatus
import com.bhm.sdk.support.utils.DownLoadUtil

/**
 * @author Buhuiming
 * @description: 下载回调
 * @date :2022/10/20 13:49
 */
class DownloadCallBack(val context: Context) : IDownLoadCallBack {

    private var _initialize: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _waiting: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _stop: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _complete: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _progress: ((dLFModel: DownLoadFileModel) -> Unit)? = null

    private var _fail: ((dLFModel: DownLoadFileModel, throwable: Throwable) -> Unit)? = null

    private var lastProgress = 0f

    fun onInitialize(value: (dLFModel: DownLoadFileModel) -> Unit) {
        _initialize = value
    }

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

    fun onFail(value: (dLFModel: DownLoadFileModel, throwable: Throwable) -> Unit) {
        _fail = value
    }

    override fun onInitialize(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.INITIAL
        dLFModel.progress = 0f
        _initialize?.invoke(dLFModel)
    }

    override fun onWaiting(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.WAITING
        _waiting?.invoke(dLFModel)
    }

    override fun onStop(dLFModel: DownLoadFileModel) {
        //处理状态和进度对应不上，以本地文件位置
        if (dLFModel.progress >= 100f) {
            if (DownLoadUtil.checkExistFullFile(
                    context,
                    dLFModel.fileName,
                    dLFModel.localParentPath
                )
            ) {
                dLFModel.status = DownLoadStatus.COMPETE
            } else {
                dLFModel.progress = DownLoadUtil.getExistFileProgress(
                    context,
                    dLFModel.fileName,
                    dLFModel.localParentPath
                )
                dLFModel.status = DownLoadStatus.STOP
            }
        } else {
            dLFModel.status = DownLoadStatus.STOP
        }
        _stop?.invoke(dLFModel)
    }

    override fun onComplete(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.COMPETE
        _complete?.invoke(dLFModel)
    }

    override fun onProgress(dLFModel: DownLoadFileModel) {
        if (lastProgress < dLFModel.progress) {
            lastProgress = dLFModel.progress

            //处理状态和进度对应不上，以本地文件位置
            if (dLFModel.progress >= 100f) {
                if (DownLoadUtil.checkExistFullFile(
                        context,
                        dLFModel.fileName,
                        dLFModel.localParentPath
                    )
                ) {
                    dLFModel.status = DownLoadStatus.COMPETE
                } else {
                    dLFModel.progress = DownLoadUtil.getExistFileProgress(
                        context,
                        dLFModel.fileName,
                        dLFModel.localParentPath
                    )
                    dLFModel.status = DownLoadStatus.DOWNING
                }
            } else {
                dLFModel.status = DownLoadStatus.DOWNING
            }
            _progress?.invoke(dLFModel)
        }
    }

    override fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable) {
        //处理状态和进度对应不上，以本地文件位置
        if (dLFModel.progress >= 100f) {
            if (DownLoadUtil.checkExistFullFile(
                    context,
                    dLFModel.fileName,
                    dLFModel.localParentPath
                )
            ) {
                dLFModel.status = DownLoadStatus.COMPETE
            } else {
                dLFModel.progress = DownLoadUtil.getExistFileProgress(
                    context,
                    dLFModel.fileName,
                    dLFModel.localParentPath
                )
                dLFModel.status = DownLoadStatus.FAIL
            }
        } else {
            dLFModel.status = DownLoadStatus.FAIL
        }
        _fail?.invoke(dLFModel, throwable)
    }
}