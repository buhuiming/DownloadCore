package com.bhm.sdk.support

import android.text.TextUtils

/**
 * @author Buhuiming
 * @description: 配置项
 * @date :2022/10/19 15:08
 */
class DownloadConfig private constructor(builder: Builder) {

    private val maxDownloadSize: Int

    private val downloadParentPath: String?

    private val writeTimeout: Int

    private val readTimeout: Int

    private val connectTimeout: Int

    companion object {
        private const val WRITE_TIMEOUT = 30
        private const val READ_TIMEOUT = 30
        private const val CONNECT_TIMEOUT = 15
        const val MAX_DOWNING_SIZE = 1 //最大同时下载数量
        const val SP_FILE_NAME = "download_list"
    }

    init {
        maxDownloadSize = builder.maxDownloadSize
        downloadParentPath = builder.downloadParentPath
        writeTimeout = builder.writeTimeout
        readTimeout = builder.readTimeout
        connectTimeout = builder.connectTimeout
    }

    fun getMaxDownloadSize(): Int {
        return if (maxDownloadSize == 0) MAX_DOWNING_SIZE else maxDownloadSize
    }

    fun getWriteTimeout(): Int {
        return if (writeTimeout == 0) WRITE_TIMEOUT else writeTimeout
    }

    fun getReadTimeout(): Int {
        return if (readTimeout == 0) READ_TIMEOUT else readTimeout
    }

    fun getConnectTimeout(): Int {
        return if (connectTimeout == 0) CONNECT_TIMEOUT else connectTimeout
    }

    fun getDownloadParentPath(): String? {
        return downloadParentPath
    }

    class Builder {

        internal var maxDownloadSize = 0

        internal var downloadParentPath: String? = null

        internal var writeTimeout = 0

        internal var readTimeout = 0

        internal var connectTimeout = 0

        fun setMaxDownloadSize(maxDownloadSize: Int): Builder {
            this.maxDownloadSize = maxDownloadSize
            return this
        }

        fun setDownloadParentPath(downloadParentPath: String?): Builder {
            this.downloadParentPath = downloadParentPath
            return this
        }

        fun setWriteTimeout(writeTimeout: Int): Builder {
            this.writeTimeout = writeTimeout
            return this
        }

        fun setReadTimeout(readTimeout: Int): Builder {
            this.readTimeout = readTimeout
            return this
        }

        fun setConnectTimeout(connectTimeout: Int): Builder {
            this.connectTimeout = connectTimeout
            return this
        }

        fun build(): DownloadConfig {
            require(!TextUtils.isEmpty(downloadParentPath)) { "下载文件的存储根地址不可为空" }
            return DownloadConfig(this)
        }
    }
}