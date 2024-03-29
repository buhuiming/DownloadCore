package com.bhm.sdk.support

import android.app.Notification
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

    private val notificationId: Int

    private var downloadInTheBackground: Notification? = null

    private var downloadOverWiFiOnly: Boolean = false

    private var logger: Boolean = false

    private var defaultHeader: HashMap<String, String>?

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
        notificationId = builder.notificationId
        downloadOverWiFiOnly = builder.downloadOverWiFiOnly
        defaultHeader = builder.defaultHeader
        logger = builder.logger
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

    fun downloadNotification(): Notification? {
        return downloadInTheBackground
    }

    fun downloadNotificationId(): Int {
        return notificationId
    }

    fun isDownloadOverWiFiOnly(): Boolean {
        return downloadOverWiFiOnly
    }

    fun isLogger(): Boolean {
        return logger
    }

    fun getDefaultHeader() = defaultHeader

    class Builder {

        internal var maxDownloadSize = 0

        internal var downloadParentPath: String? = null

        internal var writeTimeout = 0L

        internal var readTimeout = 0L

        internal var connectTimeout = 0L

        internal var notificationId = 0

        internal var downloadInTheBackground: Notification? = null

        internal var downloadOverWiFiOnly: Boolean = false

        internal var logger: Boolean = false

        internal var defaultHeader: HashMap<String, String>? = null

        fun setMaxDownloadSize(maxDownloadSize: Int) = apply {
            this.maxDownloadSize = maxDownloadSize
        }

        fun setDownloadParentPath(downloadParentPath: String?) = apply {
            this.downloadParentPath = downloadParentPath
        }

        fun setWriteTimeout(writeTimeout: Long) = apply {
            this.writeTimeout = writeTimeout
        }

        fun setReadTimeout(readTimeout: Long) = apply {
            this.readTimeout = readTimeout
        }

        fun setConnectTimeout(connectTimeout: Long) = apply {
            this.connectTimeout = connectTimeout
        }

        fun setDownloadOverWiFiOnly(downloadOverWiFiOnly: Boolean) = apply {
            this.downloadOverWiFiOnly = downloadOverWiFiOnly
        }

        fun setLogger(logger: Boolean) = apply {
            this.logger = logger
        }

        fun setDownloadInTheBackground(downloadInTheBackground: Notification?, notificationId: Int) = apply {
            this.downloadInTheBackground = downloadInTheBackground
            this.notificationId = notificationId
        }

        /**
         * 设置请求默认的header
         */
        fun setDefaultHeader(defaultHeader: HashMap<String, String>?) = apply {
            this.defaultHeader = defaultHeader
        }

        fun build(): DownloadConfig {
            require(!TextUtils.isEmpty(downloadParentPath)) { "下载文件的存储根地址不可为空" }
            return DownloadConfig(this)
        }
    }
}