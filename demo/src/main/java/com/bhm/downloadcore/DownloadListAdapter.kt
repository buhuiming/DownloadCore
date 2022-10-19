package com.bhm.downloadcore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bhm.downloadcore.databinding.RecyclerItemDownloadBinding
import com.bhm.sdk.support.DownLoadFileModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * @author Buhuiming
 * @date :2022/10/19 13:54
 */
class DownloadListAdapter (
    layoutResId: Int,
    data: MutableList<DownLoadFileModel>?
) : BaseQuickAdapter<DownLoadFileModel, DownloadListAdapter.VH>(layoutResId, data) {

    class VH(
        parent: ViewGroup,
        val binding: RecyclerItemDownloadBinding = RecyclerItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ),
    ) : BaseViewHolder(binding.root)

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent)
    }

    override fun convert(holder: VH, item: DownLoadFileModel) {
        item.let {

        }
    }
}