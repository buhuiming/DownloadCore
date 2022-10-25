package com.bhm.sdk.support.interfaces

import com.bhm.sdk.support.DownLoadFileModel

/**
 * * @author Buhuiming
 * * @description: 下载回调
 * * @date :2022/10/19 16:49
 */
interface IDownLoadCallBack {

    fun onInitialize(dLFModel: DownLoadFileModel)

    fun onWaiting(dLFModel: DownLoadFileModel)

    fun onStop(dLFModel: DownLoadFileModel)

    fun onComplete(dLFModel: DownLoadFileModel)

    fun onProgress(dLFModel: DownLoadFileModel)

    fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable)
}