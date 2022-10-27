package com.bhm.sdk.support.utils

import android.content.Context
import android.text.TextUtils
import com.bhm.sdk.support.DownloadConfig
import java.io.File
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
    fun getExistFileProgress(context: Context, fileName: String, parentPath: String): Float {
        val length = SPUtil[context, DownloadConfig.SP_FILE_NAME, fileName, 0L] as Long
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
    fun checkExistFullFile(context: Context, fileName: String, parentPath: String): Boolean {
        val length = SPUtil[context, DownloadConfig.SP_FILE_NAME, fileName, 0L] as Long
        if (length > 0) {
            val file = File(parentPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val downFile = File(file, fileName)
            if (downFile.length() == length) {
                //已经下载过了
                return true
            }
        }
        return false
    }

    /**
     * @apiNote 获取下载文件名字
     * @author buhuiming
     */
    fun generateFileName(url: String): String {
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

    /**
     * 清空文件夹
     */
    fun clearDir(dir: File?, isDeleteDir: Boolean) {
        dir?.let {
            if (it.exists()) { // 判断文件是否存在
                if (it.isFile) { // 判断是否是文件
                    it.delete() // 删除文件
                } else if (it.isDirectory) { // 否则如果它是一个目录
                    val files = it.listFiles() // 声明目录下所有的文件 files[];
                    files?.indices?.forEach { i ->
                        // 遍历目录下所有的文件
                        clearDir(files[i], true) // 把每个文件用这个方法进行迭代
                    }
                    if (isDeleteDir) {
                        it.delete() // 删除文件夹
                    }
                }
            }
        }
    }
}