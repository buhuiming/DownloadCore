package com.bhm.sdk.support.net

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build

/**
 * @author Buhuiming
 * @description: 监听网络变化，Android5.0或以上的版本
 * @date :2022/10/25 17:27
 */
class ConnectivityEngine(context: Context) {

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var networkCallback: DownloadNetworkCallback? = null

    fun registerNetworkChangeListener(callback: (Boolean, Network) -> Unit) {
        val networkCallback = DownloadNetworkCallback(connectivityManager, callback).also { this.networkCallback = it }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 如果Android版本等于7.0(API 24)或以上
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val networkRequest = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }
    }

    fun unregisterNetworkChangeListener() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }
}