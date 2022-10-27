package com.bhm.sdk.support.observer

import com.bhm.sdk.support.DownLoadFileModel
import com.bhm.sdk.support.interfaces.IDownLoadCallBack

/**
 * @author Buhuiming
 * @description: 下载回调被观察者
 * @date :2022/10/27 9:47
 */
internal class DownloadObservable : IDownLoadCallBack{

    private val observers = ArrayList<HashMap<String, DownloadObserver?>>()

    fun addDownloadObserver(fileName: String, observer: DownloadObserver?) {
        val map = HashMap<String, DownloadObserver?>()
        map[fileName] = observer
        observers.add(map)
    }

    fun removeDownloadObserver(fileName: String) {
        val iterator = observers.iterator()
        while (iterator.hasNext()) {
            val map = iterator.next()
            if (map.keys.first() == fileName) {
                iterator.remove()
            }
        }
    }

    fun removeAllDownloadObserver() {
        observers.clear()
    }

    fun hasObservers() = observers.size > 0

    override fun onInitialize(dLFModel: DownLoadFileModel) {
        observers.forEach {
            if (it.keys.first() == dLFModel.fileName) {
                it.values.first()?.onInitialize(dLFModel)
            }
        }
    }

    override fun onWaiting(dLFModel: DownLoadFileModel) {
        observers.forEach {
            if (it.keys.first() == dLFModel.fileName) {
                it.values.first()?.onWaiting(dLFModel)
            }
        }
    }

    override fun onStop(dLFModel: DownLoadFileModel) {
        observers.forEach {
            if (it.keys.first() == dLFModel.fileName) {
                it.values.first()?.onStop(dLFModel)
            }
        }
    }

    override fun onComplete(dLFModel: DownLoadFileModel) {
        observers.forEach {
            if (it.keys.first() == dLFModel.fileName) {
                it.values.first()?.onComplete(dLFModel)
            }
        }
    }

    override fun onProgress(dLFModel: DownLoadFileModel) {
        observers.forEach {
            if (it.keys.first() == dLFModel.fileName) {
                it.values.first()?.onProgress(dLFModel)
            }
        }
    }

    override fun onFail(dLFModel: DownLoadFileModel, throwable: Throwable) {
        observers.forEach {
            if (it.keys.first() == dLFModel.fileName) {
                it.values.first()?.onFail(dLFModel, throwable)
            }
        }
    }
}