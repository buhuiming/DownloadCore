package com.bhm.sdk.support

import java.io.InputStream

/**
 * * @author Buhuiming
 * * @description: 下载回调
 * * @date :2022/10/19 16:49
 */
interface IDownLoadCallBack {

    fun onWaiting(dLFModel: DownLoadFileModel)

    fun onStop(dLFModel: DownLoadFileModel)

    fun onComplete(dLFModel: DownLoadFileModel)

    fun onProgress(dLFModel: DownLoadFileModel)

    fun saveFile(dLFModel: DownLoadFileModel)

    fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable)
}