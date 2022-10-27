package com.bhm.sdk.support

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * @author Buhuiming
 * @description:
 * @date :2022/10/26 17:17
 */
class WeakHandler(looper: Looper?, cb: Callback?) : Handler(looper!!) {

    private var activityWeakReference: WeakReference<Callback?>

    init {
        activityWeakReference = WeakReference<Callback?>(cb)
    }

    fun setCallback(cb: Callback?) {
        activityWeakReference = WeakReference<Callback?>(cb)
    }

    override fun handleMessage(msg: Message) {
        val cb = activityWeakReference.get()
        cb?.handleMessage(msg)
    }
}