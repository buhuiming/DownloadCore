package com.bhm.sdk.support

import android.content.Context
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * 工具类
 * @author buhuiming
 * @date :2022/10/19 16:19
 */
object DownLoadUtil {

    /**
     * @apiNote 获取已下载文件的进度
     * @author buhuiming
     */
    fun getExistFileProgress(context: Context, url: String, parentPath: String): Float {
        val fileName = getMD5FileName(url)
        val length = SPUtil[context, DownloadConfig.SP_FILE_NAME, url, 0L] as Long
        if (length > 0) {
            val file = File(parentPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val downFile = File(file, fileName)
            val progress = downFile.length() * 100f / length
            return String.format("%.1f", progress).toFloat()
        }
        return 0f
    }

    /**
     * @apiNote 根据URL检测本地是否有已下载完成的文件
     * @author buhuiming
     */
    fun checkExistFullFile(context: Context, url: String, parentPath: String): Boolean {
        val fileName = getMD5FileName(url)
        val length = SPUtil[context, DownloadConfig.SP_FILE_NAME, url, 0L] as Long
        if (length > 0) {
            val file = File(parentPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val downFile = File(file, fileName)
            if (downFile.length() == length) {
                //已经下载过了
                Log.e("DownLoadUtils", "open exist file.")
                return true
            }
        }
        return false
    }

    /**
     * @apiNote 获取下载文件名字
     * @author buhuiming
     */
    fun getMD5FileName(url: String): String {
        return getMD5(url)
    }

    private fun getMD5(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return ""
        }
        var messageDigest: MessageDigest? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            messageDigest.reset()
            messageDigest.update(str.toByteArray(StandardCharsets.UTF_8))
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        if (messageDigest == null) {
            return ""
        }
        val byteArray = messageDigest.digest()
        val md5StrBuff = StringBuilder()
        for (b in byteArray) {
            if (Integer.toHexString(0xFF and b.toInt()).length == 1) md5StrBuff.append("0").append(
                Integer.toHexString(0xFF and b.toInt())
            ) else md5StrBuff.append(Integer.toHexString(0xFF and b.toInt()))
        }
        return md5StrBuff.toString()
    }
}