package com.bhm.downloadcore

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhm.downloadcore.databinding.ActivityMainBinding
import com.bhm.sdk.support.DownLoadFileModel
import com.bhm.sdk.support.DownloadConfig
import com.bhm.sdk.support.DownloadRequest
import com.bhm.support.sdk.common.BaseVBActivity
import com.bhm.support.sdk.common.BaseViewModel
import com.bhm.support.sdk.core.AppTheme

class MainActivity : BaseVBActivity<BaseViewModel, ActivityMainBinding>() {

    private var listAdapter: DownloadListAdapter? = null

    private var downloadRequest: DownloadRequest? = null

    override fun createViewModel() = BaseViewModel(application)

    override fun initData() {
        super.initData()
        AppTheme.setStatusBarColor(this, R.color.purple_500)
        initList()
        initDownloadManager()
    }

    private fun initList() {
        viewBinding.recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        viewBinding.recyclerView.layoutManager = mLayoutManager
        viewBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        //解决RecyclerView局部刷新时闪烁
        (viewBinding.recyclerView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        listAdapter = DownloadListAdapter(0, downloadList())
        viewBinding.recyclerView.adapter = listAdapter
    }

    private fun downloadList(): MutableList<DownLoadFileModel> {
        val list = arrayListOf<DownLoadFileModel>()
        for (i in 0..5) {
            list.add(DownLoadFileModel())
        }
        return list
    }

    private fun initDownloadManager() {
        downloadRequest = DownloadRequest()
        val downloadConfig: DownloadConfig = DownloadConfig.Builder()
            .setMaxDownloadSize(3)
            .setWriteTimeout(30)
            .setReadTimeout(30)
            .setConnectTimeout(15)
            .setDownloadParentPath(getExternalFilesDir("downloadFiles")?.absolutePath)
            .build()
        downloadRequest?.newRequest(downloadConfig)
    }
}