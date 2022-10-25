package com.bhm.sdk.support.net

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * @author Buhuiming
 * @description: 监听网络变化，Android5.0或以上的版本
 * @date :2022/10/25 17:24
 */
class DownloadNetworkCallback(
    private val connectivityManager: ConnectivityManager,
    private val callback: (Boolean, Network) -> Unit
): ConnectivityManager.NetworkCallback() {

    private var isCalledOnLost = true
    private var isCalledOnAvailable = true
    private var uiHandler = Handler(Looper.getMainLooper())

    /** 在框架连接并声明可以使用新网络时调用。(在有网络的情况下注册监听器后这个函数就会立马被调用) */
    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.i("DownloadNetworkCallback", "onAvailable被调用，isCalledOnLost = $isCalledOnLost")
        /*
           网络切换时，拿connectivityManager.activeNetwork来判断是不准确的，
           比如从一个wifi切换到另一个wifi时会先断开当前wifi，然后立马回调onAvailable，网络变成了移动数据网络。
           之后连上另一个wifi时又回调onAvailable，网络变成了wifi网络。
           在变成移动数据网络的时候，拿connectivityManager.activeNetwork来判断是否是移动网络显示为false，
           而使用onAvailable中的参数network来判断则为true。
         */
        connectivityManager.getNetworkCapabilities(network)?.let { capabilities ->
            val isCurrentWifiNetwork = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isCurrentMobileNetwork = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val isCurrentVpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            val isInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                // 插有sim卡且没连wifi时isCellular为true，此时连上wifi，isCellular为false。
                Log.i("DownloadNetworkCallback","onAvailable：isInternet = $isInternet, isValidated = $isValidated" +
                        ", isWifi = $isCurrentWifiNetwork, isCellular = $isCurrentMobileNetwork, isVPN = $isCurrentVpn")
            } else {
                Log.i("DownloadNetworkCallback","onAvailable：isInternet = $isInternet, isWifi = $isCurrentWifiNetwork" +
                        ", isCellular = $isCurrentMobileNetwork, isVPN = $isCurrentVpn")
            }
        }

        // 在启动VPN时(在Android10上会这样，在Android7.1.1不会有任何的方法回调)，启动成功后会直接再回调onAvailable方法(不回调onLost)，网络变成WIFI + VPN或进CELLULAR + VPN
        // 注：不要使用VpnManager.isOnline()来判断，这个并不准确。从wifi vpn切换到APN网时，VPN图标还在，而且VpnManager.isOnline()返回true，实际上此时的VPN已经失效
        // 切换网络时，如果VPN没有断开，则不需要做任何处理，因为ip会使用VPN的ip，数据请求权限什么都不会变的。
        // 注：在android10系统中，切换网络后如果VPN没有断开，则不会执行onAvailable方法回调
        // 在Android7.1.1 VPN连着时，切换网络VPN就会断开，在Android10不会断开，但是在Android10不回调onAvailable（注：Android10启动VPN成功后会再回调此方法，这种情况也是不需要执行后面代码的）
        // 从移动数据网络切换到wifi网络，此时系统不会回调onLost方法，而是直接回调onAvailable。此时再断开Wifi会回调onLost方法，接着回调onAvailable变成移动数据网络
        // 在Android10（在Android7.1.1中试验是会调用onAvailable的,因为切换时VPN会断开），开着VPN，移动数据网络切换到wifi，只回调onCapabilitiesChanged方法。如果没开VPN，则会回调onAvailable方法。
        // WIFI开着VPN切换到移动网也只回调onCapabilitiesChanged
        // 开着VPN切换网络只回调onCapabilitiesChanged的前提应该是切换后VPN并没有断开，如果断开了应该会回调onAvailable方法
        // 从一个wifi切换到另一个wifi，先回调onLost，然后onAvailable是移动网络，wifi连上后又回调一次onAvailable是wifi网络

        isCalledOnAvailable = true
        if (!isCalledOnLost) {
            return // 如果没有调用过onLost函数，则不往下执行了，预防onAvailable函数连续执行多次的情况
        }
        isCalledOnLost = false // 预防onAvailable函数连续执行多次的情况
        Log.i("DownloadNetworkCallback","有网了")
        uiHandler.post { callback(true, network) }
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        Log.i("DownloadNetworkCallback","onCapabilitiesChanged，networkCapabilities = $networkCapabilities")
    }

    /** 当网络断开连接或不再满足此请求或回调时调用。(在无网络的情况下注册监听器后这个函数不会被调用) */
    override fun onLost(network: Network) {
        super.onLost(network)
        Log.i("DownloadNetworkCallback","onLost被调用，isCalledOnAvailable = $isCalledOnAvailable")

        isCalledOnLost = true
        if (!isCalledOnAvailable) {
            return // 如果没有调用过onAvailable函数，则不往下执行了，预防onLost函数连续执行多次的情况
        }
        isCalledOnAvailable = false // 预防onLost函数连续执行多次的情况
        Log.i("DownloadNetworkCallback","没网了")
        uiHandler.post { callback(false, network) }
    }
}