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

    private val writeTimeout: Long

    private val readTimeout: Long

    private val connectTimeout: Long

    private val downloadInTheBackground: Boolean

    companion object {
        private const val WRITE_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val CONNECT_TIMEOUT = 15L
        const val MAX_DOWNING_SIZE = 1 //最大同时下载数量
        const val SP_FILE_NAME = "download_list"
    }

    init {
        maxDownloadSize = builder.maxDownloadSize
        downloadParentPath = builder.downloadParentPath
        writeTimeout = builder.writeTimeout
        readTimeout = builder.readTimeout
        connectTimeout = builder.connectTimeout
        downloadInTheBackground = builder.downloadInTheBackground
    }

    fun getMaxDownloadSize(): Int {
        return if (maxDownloadSize == 0) MAX_DOWNING_SIZE else maxDownloadSize
    }

    fun getWriteTimeout(): Long {
        return if (writeTimeout == 0L) WRITE_TIMEOUT else writeTimeout
    }

    fun getReadTimeout(): Long {
        return if (readTimeout == 0L) READ_TIMEOUT else readTimeout
    }

    fun getConnectTimeout(): Long {
        return if (connectTimeout == 0L) CONNECT_TIMEOUT else connectTimeout
    }

    fun getDownloadParentPath(): String? {
        return downloadParentPath
    }

    fun getDownloadInTheBackground(): Boolean {
        return downloadInTheBackground
    }

    class Builder {

        internal var maxDownloadSize = 0

        internal var downloadParentPath: String? = null

        internal var writeTimeout = 0L

        internal var readTimeout = 0L

        internal var connectTimeout = 0L

        internal var downloadInTheBackground = false

        fun setMaxDownloadSize(maxDownloadSize: Int): Builder {
            this.maxDownloadSize = maxDownloadSize
            return this
        }

        fun setDownloadParentPath(downloadParentPath: String?): Builder {
            this.downloadParentPath = downloadParentPath
            return this
        }

        fun setWriteTimeout(writeTimeout: Long): Builder {
            this.writeTimeout = writeTimeout
            return this
        }

        fun setReadTimeout(readTimeout: Long): Builder {
            this.readTimeout = readTimeout
            return this
        }

        fun setConnectTimeout(connectTimeout: Long): Builder {
            this.connectTimeout = connectTimeout
            return this
        }

        fun setDownloadInTheBackground(downloadInTheBackground: Boolean): Builder {
            this.downloadInTheBackground = downloadInTheBackground
            return this
        }

        fun build(): DownloadConfig {
            require(!TextUtils.isEmpty(downloadParentPath)) { "下载文件的存储根地址不可为空" }
            return DownloadConfig(this)
        }
    }
}