package com.bhm.downloadcore

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bhm.downloadcore.databinding.RecyclerItemDownloadBinding
import com.bhm.sdk.support.DownLoadStatus
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * @author Buhuiming
 * @date :2022/10/19 13:54
 */
class DownloadListAdapter (
    layoutResId: Int,
    data: MutableList<FileModel>?
) : BaseQuickAdapter<FileModel, DownloadListAdapter.VH>(layoutResId, data) {

    class VH(
        parent: ViewGroup,
        val binding: RecyclerItemDownloadBinding = RecyclerItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : BaseViewHolder(binding.root)

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun convert(holder: VH, item: FileModel) {
        item.let {
            holder.binding.tvName.text = item.fileName
            when (item.status) {
                DownLoadStatus.INITIAL -> {
                    holder.binding.btnRestart.isEnabled = false
                    holder.binding.btnChange.isEnabled = true
                    holder.binding.btnChange.text = "未开始"
                }
                DownLoadStatus.WAITING -> {
                    holder.binding.btnRestart.isEnabled = false
                    holder.binding.btnChange.isEnabled = true
                    holder.binding.btnChange.text = "等待中"
                }
                DownLoadStatus.STOP -> {
                    holder.binding.btnRestart.isEnabled = true
                    holder.binding.btnChange.isEnabled = true
                    holder.binding.btnChange.text = buildString {
                        append("停止")
                        append(item.progress)
                        append("%")
                    }
                }
                DownLoadStatus.DOWNING -> {
                    holder.binding.btnRestart.isEnabled = false
                    holder.binding.btnChange.isEnabled = true
                    holder.binding.btnChange.text = buildString {
                        append("下载")
                        append(item.progress)
                        append("%")
                    }
                }
                DownLoadStatus.COMPETE -> {
                    holder.binding.btnRestart.isEnabled = true
                    holder.binding.btnChange.isEnabled = true
                    holder.binding.btnChange.text = "下载完成"
                }
                DownLoadStatus.FAIL -> {
                    holder.binding.btnRestart.isEnabled = true
                    holder.binding.btnChange.isEnabled = true
                    holder.binding.btnChange.text = buildString {
                        append("失败")
                        append(item.progress)
                        append("%")
                    }
                }
                else -> {}
            }
        }
    }
}