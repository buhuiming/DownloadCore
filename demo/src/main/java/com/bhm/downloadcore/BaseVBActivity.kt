package com.bhm.downloadcore

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.viewbinding.ViewBinding

/**
 * @author Buhuiming
 * @description: ViewBinding基类
 * @date :2022/6/28 14:38
 */
abstract class BaseVBActivity<VM : BaseViewModel, B : ViewBinding> : BaseActivity<VM>() {

    lateinit var viewBinding: B

    lateinit var rootView: View

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ViewUtil.inflateWithGeneric(this, layoutInflater)
        rootView = viewBinding.root
        setContentView(rootView)
        initData()
        initEvent()
    }

    protected open fun initData() {}

    protected open fun initEvent() {}
}