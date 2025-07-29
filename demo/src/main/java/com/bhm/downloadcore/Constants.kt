package com.bhm.downloadcore

import android.content.Context
import android.os.Build

/**
 * @author Buhuiming
 * @description:
 * @date :2022/10/20 9:49
 */
object Constants {
    private const val url = "https://gdown.baidu.com/data/wisegame/e229ae333a7d62b8/6debe229ae333a7d62b8a17b439a9b45.apk"
    val urls = arrayOf(
        url,
        url,
        url,
        "https://mraw.bus365.cn/files/group1/M00/00/F8/wKgDEl-X5nGAQMxkAe7MXjwMMyU347.apk",
        "https://dldir1.qq.com/weixin/android/weixin8015android2020_arm64.apk",
        "https://down.qq.com/qqweb/QQ_1/android_apk/Android_8.8.38.6590_537100432.apk",
        "https://zlink.toutiao.com/kG12?apk=1"
    )
    const val NOTIFICATION_ID = 111111

    const val DOWNLOAD_OVER_WIFI_ONLY = true

    const val DOWNLOAD_IN_THE_BACKGROUND = true

    fun getReadPermissionArray(context: Context): Array<String> {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val targetSdkVersion = context.applicationInfo.targetSdkVersion
                return when {
                    targetSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        arrayOf(
                            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
                            "android.permission.READ_MEDIA_IMAGES",
                            "android.permission.READ_MEDIA_VIDEO"
                        )
                    }
                    targetSdkVersion == Build.VERSION_CODES.TIRAMISU -> {
                        arrayOf(
                            "android.permission.READ_MEDIA_IMAGES",
                            "android.permission.READ_MEDIA_VIDEO"
                        )
                    }
                    else -> {
                        arrayOf("android.permission.READ_EXTERNAL_STORAGE")
                    }
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val targetSdkVersion = context.applicationInfo.targetSdkVersion
                return if (targetSdkVersion >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(
                        "android.permission.READ_MEDIA_IMAGES",
                        "android.permission.READ_MEDIA_VIDEO"
                    )
                else
                    arrayOf("android.permission.READ_EXTERNAL_STORAGE")
            }
            else -> return arrayOf(
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
            )
        }
    }
}