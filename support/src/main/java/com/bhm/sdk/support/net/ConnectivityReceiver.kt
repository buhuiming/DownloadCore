@file:Suppress("DEPRECATION")

package com.bhm.sdk.support.net

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log

/**
 * @author Buhuiming
 * @description: 监听网络变化，小于Android5.0的版本
 * @date :2022/10/25 17:20
 */
class ConnectivityReceiver : BroadcastReceiver() {

    /** 指示是否没有网络 */
    private var noConnectivity = false
    private var isFirst = true

    override fun onReceive(context: Context, intent: Intent) {
        // 网络连接状态发生变化（Wifi、蜂窝），默认连接已建立或丢失。
        // 受影响的网络的NetworkInfo作为附加发送。应该咨询一下发生了哪种连接事件。

        // 由于NetworkInfo可能因UID而异，因此应用程序应始终通过getActiveNetworkInfo（）获取网络信息。
        val networkInfo: NetworkInfo? = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO)
        Log.i("ConnectivityReceiver", "networkInfo = $networkInfo")

        // ConnectivityManager.EXTRA_NETWORK_TYPE 此字段需求api为17，当前最小为15
        val networkType = intent.getIntExtra("networkType", -1)
        Log.i("ConnectivityReceiver", "networkType = $networkType")

        // 该关键字可提供（可选）有关网络状态的额外信息。信息可以从较低的网络层传递，并且含义可能特定于特定的网络类型。
        val extraInfo = intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO)
        Log.i("ConnectivityReceiver", "extraInfo = $extraInfo")

        // 用于指示是否完全缺乏连通性，即没有网络可用
        val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
        Log.i("ConnectivityReceiver", "noConnectivity = $noConnectivity")

        // ConnectivityManager.EXTRA_INET_CONDITION 隐藏字段，该键提供有关我们与整个Internet的连接的信息。 0表示没有连接，100表示连接良好
        val inetCondition = intent.getIntExtra("inetCondition", -1)
        Log.i("ConnectivityReceiver", "inetCondition = $inetCondition")

        Log.i("ConnectivityReceiver", "网络发生变化了，noConnectivity = $noConnectivity")

        if (isFirst) {
            // 第一次的网络状态，状态怎样都是要执行的
            this.noConnectivity = noConnectivity
            isFirst = false
        } else if (this.noConnectivity == noConnectivity) {
            // 非第一次的网络状态，需要判断是否与上一次一样，一样的话就不要重复执行了
            // 数据网络切WIFI时不会回调网络断开，而是直接回调网络有效，所以需要增加一个判断：如果之前是数据网络，
            // 现在变成了Wifi，则即使网络有连网状态和之前一样也要走后面的代码
            return
        }

        this.noConnectivity = noConnectivity

        if (noConnectivity) {
            //没网了
        } else {
            //有网了
        }
    }

}