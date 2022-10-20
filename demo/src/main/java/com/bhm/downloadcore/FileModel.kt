package com.bhm.downloadcore

import com.bhm.sdk.support.DownLoadStatus
import java.io.File

/**
 * 下载文件的数据
 * @author buhuiming
 * @since 2022/10/20 v1.0.0
 */
data class FileModel(

    var downLoadUrl: String? = null, //下载链接

    var fileName: String? = null, //文件名

    var status: DownLoadStatus? = DownLoadStatus.INITIAL, //下载状态

    var progress: Float = 0f, //下载进度，保留一位小数点

)