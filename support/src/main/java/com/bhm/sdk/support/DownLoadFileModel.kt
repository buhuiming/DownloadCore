package com.bhm.sdk.support

import com.bhm.sdk.support.DownLoadStatus
import java.io.File

/**
 * 下载文件的数据
 * @author buhuiming
 * @since 2022/10/19 v1.0.0
 */
data class DownLoadFileModel(

    var downLoadUrl: String = "", //下载链接

    var localParentPath: String = "", //本地存储地址，不包含名字

    var localPath: String? = null,//本地存储地址

    var fileName: String? = null, //文件名

    var downLoadFile: File? = null,

    var status: DownLoadStatus = DownLoadStatus.INITIAL, //下载状态

    var downLoadLength: Long = 0, //已下载长度

    var totalLength: Long = 0, //总长度

    var progress: Float = 0f, //下载进度，保留一位小数点

)