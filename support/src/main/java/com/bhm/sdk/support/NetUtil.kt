package com.bhm.sdk.support

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * @author Buhuiming
 * @description:
 * @date :2022/10/24 10:49
 */
object NetUtil {

    /**
     * 判断wifi是否已连接并可用
     */
    @Suppress("DEPRECATION")
    @JvmStatic
    fun isWifiConnected(context: Context): Boolean {
        val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = mConnectivityManager.getNetworkCapabilities(mConnectivityManager.activeNetwork)
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?: false
        } else {
            val wifiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            wifiNetworkInfo?.isAvailable?: false
        }
    }

    /**
     * 判断网络是否可用
     */
    @Suppress("DEPRECATION")
    @JvmStatic
    fun isNetWorkConnected(context: Context): Boolean {
        val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isNetWorkConnected: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //获取网络属性
            val networkCapabilities = mConnectivityManager.getNetworkCapabilities(mConnectivityManager.activeNetwork)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?: false
        } else {
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            mNetworkInfo?.isAvailable?: false
        }
        return isNetWorkConnected
    }
}