package com.bhm.sdk.support.observer

import android.content.Context
import com.bhm.sdk.support.DownLoadFileModel
import com.bhm.sdk.support.DownLoadStatus
import com.bhm.sdk.support.interfaces.IDownLoadCallBack
import com.bhm.sdk.support.utils.DownLoadUtil

/**
 * @author Buhuiming
 * @description: 下载回调观察者
 * @date :2022/10/20 13:49
 */
open class DownloadObserver(val context: Context) : IDownLoadCallBack {

    override fun onInitialize(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.INITIAL
        dLFModel.progress = 0f
    }

    override fun onWaiting(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.WAITING
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
    }

    override fun onComplete(dLFModel: DownLoadFileModel) {
        dLFModel.status = DownLoadStatus.COMPETE
    }

    override fun onProgress(dLFModel: DownLoadFileModel) {
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
    }
}