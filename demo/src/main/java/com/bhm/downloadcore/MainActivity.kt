package com.bhm.downloadcore

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhm.downloadcore.databinding.ActivityMainBinding
import com.bhm.sdk.support.DownLoadStatus
import com.bhm.support.sdk.common.BaseVBActivity
import com.bhm.support.sdk.core.AppTheme
import com.bhm.support.sdk.interfaces.PermissionCallBack

class MainActivity : BaseVBActivity<MainViewModel, ActivityMainBinding>() {

    private var listAdapter: DownloadListAdapter? = null

    override fun createViewModel() = MainViewModel(application)

    override fun initData() {
        super.initData()
        AppTheme.setStatusBarColor(this, R.color.purple_500)
        initList()
        initObserver()
        requestPermission(Constants.PERMISSION_REQUEST_STORAGE, object : PermissionCallBack {
            override fun agree() {

            }

            override fun refuse(refusePermissions: ArrayList<String>) {
                finish()
            }
        })
    }

    override fun initEvent() {
        super.initEvent()
        viewBinding.btnAllStart.setOnClickListener { viewModel.startAllDownloads() }
        viewBinding.btnAllPause.setOnClickListener { viewModel.pauseAllDownloads() }
        viewBinding.btnAllDelete.setOnClickListener { viewModel.deleteAllDownloads() }
        listAdapter?.addChildClickViewIds(R.id.btnRestart, R.id.btnChange)
        listAdapter?.setOnItemChildClickListener { adapter, view, position ->
            val model = adapter.data[position] as FileModel
            when (view.id) {
                R.id.btnRestart -> {
                    viewModel.restartDownload(model.downLoadUrl)
                }
                R.id.btnChange -> {
                    when (model.status) {
                        DownLoadStatus.INITIAL , DownLoadStatus.STOP -> {
                            model.status = DownLoadStatus.DOWNING
                            adapter.notifyItemChanged(position)
                            viewModel.startDownload(model.downLoadUrl)
                        }
                        DownLoadStatus.WAITING -> {
                            viewModel.deleteDownload(model.downLoadUrl)
                        }
                        DownLoadStatus.DOWNING -> {
                            viewModel.pauseDownload(model.downLoadUrl)
                        }
                        DownLoadStatus.COMPETE -> {
                            //打开
                            viewModel.openFile(model.fileName)
                        }
                        DownLoadStatus.FAIL -> {
                            viewModel.restartDownload(model.downLoadUrl)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun initList() {
        viewBinding.recyclerView.setHasFixedSize(true)
        val mLayoutManager = LinearLayoutManager(this)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        viewBinding.recyclerView.layoutManager = mLayoutManager
        viewBinding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        //解决RecyclerView局部刷新时闪烁
        (viewBinding.recyclerView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
        listAdapter = DownloadListAdapter(0, arrayListOf())
        viewBinding.recyclerView.adapter = listAdapter
    }

    private fun initObserver() {
        viewModel.initDownloadManager()
        viewModel.downloadList.observe(this, this::refresh)
        viewModel.initDownloadList()
    }

    private fun refresh(downloadList: MutableList<FileModel>?) {
        var isAllStart = false
        var isAllPause = false
        var isAllDelete = false
        downloadList?.forEach {
            when (it.status) {
                DownLoadStatus.INITIAL -> {
                    isAllStart = true
                }
                DownLoadStatus.STOP -> {
                    isAllStart = true
                    isAllDelete = true
                }
                DownLoadStatus.FAIL -> {
                    isAllStart = true
                    isAllDelete = true
                }
                DownLoadStatus.WAITING -> {
                    isAllPause = true
                    isAllDelete = true
                }
                DownLoadStatus.DOWNING -> {
                    isAllPause = true
                    isAllDelete = true
                }
                DownLoadStatus.COMPETE -> {
                    isAllDelete = true
                }
                else -> {}
            }
        }
        if (downloadList?.isEmpty() == true) {
            isAllStart = false
            isAllPause = false
            isAllDelete = false
        } else {
            listAdapter?.let {
                if (it.data.size == 0) {
                    it.data = downloadList!!
                    it.notifyItemRangeInserted(0, downloadList.size)
                } else {
                    it.data = downloadList!!
                    it.notifyItemRangeChanged(0, downloadList.size)
                }
            }
        }
        viewBinding.btnAllStart.isEnabled = isAllStart
        viewBinding.btnAllPause.isEnabled = isAllPause
        viewBinding.btnAllDelete.isEnabled = isAllDelete
    }
}