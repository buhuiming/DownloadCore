@file:Suppress("SENSELESS_COMPARISON")

package com.bhm.sdk.support.observer

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.bhm.sdk.support.DownLoadFileModel
import com.bhm.sdk.support.WeakHandler

/**
 * @author Buhuiming
 * @description: 管理下载监听和回调
 * @date :2022/10/27 9:49
 */
internal class DownloadEngine : Handler.Callback{

    private var downloadObservable: DownloadObservable? = null

    private var mainHandler: WeakHandler? = null

    companion object {

        private var instance: DownloadEngine = DownloadEngine()

        private const val INITIAL = 1

        private const val WAITING = 2

        private const val STOP = 3

        private const val DOWNING = 4

        private const val COMPETE = 5

        private const val FAIL = 6

        fun get(): DownloadEngine {
            if (instance == null) {
                synchronized(DownloadEngine::class.java) {
                    if (instance == null) {
                        instance = DownloadEngine()
                    }
                }
            }
            return instance
        }
    }

    init {
        downloadObservable = DownloadObservable()
        mainHandler = WeakHandler(Looper.getMainLooper(), this)
    }

    fun register(fileName: String, observer: DownloadObserver?) {
        downloadObservable?.addDownloadObserver(fileName, observer)
    }

    fun unRegister(fileName: String) {
        downloadObservable?.removeDownloadObserver(fileName)
    }

    fun close() {
        downloadObservable?.removeAllDownloadObserver()
        mainHandler?.removeCallbacksAndMessages(null)
    }

    internal fun onInitialize(dLFModel: DownLoadFileModel) {
        if (downloadObservable?.hasObservers() == true) {
            val message = Message.obtain()
            message.what = INITIAL
            message.obj = dLFModel
            mainHandler?.sendMessage(message)
        }
    }

    internal fun onWaiting(dLFModel: DownLoadFileModel) {
        if (downloadObservable?.hasObservers() == true) {
            val message = Message.obtain()
            message.what = WAITING
            message.obj = dLFModel
            mainHandler?.sendMessage(message)
        }
    }

    internal fun onStop(dLFModel: DownLoadFileModel) {
        if (downloadObservable?.hasObservers() == true) {
            val message = Message.obtain()
            message.what = STOP
            message.obj = dLFModel
            mainHandler?.sendMessage(message)
        }
    }

    internal fun onComplete(dLFModel: DownLoadFileModel) {
        if (downloadObservable?.hasObservers() == true) {
            val message = Message.obtain()
            message.what = COMPETE
            message.obj = dLFModel
            mainHandler?.sendMessage(message)
        }
    }

    internal fun onProgress(dLFModel: DownLoadFileModel) {
        if (downloadObservable?.hasObservers() == true) {
            val message = Message.obtain()
            message.what = DOWNING
            message.obj = dLFModel
            mainHandler?.sendMessage(message)
        }
    }

    internal fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable) {
        if (downloadObservable?.hasObservers() == true) {
            val message = Message.obtain()
            val result: Array<Any?> = Array(2){}
            result[0] = dLFModel
            result[1] = throwable
            message.what = FAIL
            message.obj = result
            mainHandler?.sendMessage(message)
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when(msg.what) {
            INITIAL -> {
                downloadObservable?.onInitialize(msg.obj as DownLoadFileModel)
            }
            WAITING -> {
                downloadObservable?.onWaiting(msg.obj as DownLoadFileModel)
            }
            STOP -> {
                downloadObservable?.onStop(msg.obj as DownLoadFileModel)
            }
            DOWNING -> {
                downloadObservable?.onProgress(msg.obj as DownLoadFileModel)
            }
            COMPETE -> {
                downloadObservable?.onComplete(msg.obj as DownLoadFileModel)
            }
            FAIL -> {
                downloadObservable?.onFail((msg.obj as Array<*>)[0] as DownLoadFileModel
                    , (msg.obj as Array<*>)[1] as Throwable)
            }
        }
        return false
    }
}