package com.bhm.downloadcore

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner


/**
 * @author Buhuiming
 * @description: Activity基类
 * @date :2022/6/28 14:09
 */
abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity(), Handler.Callback {

    lateinit var viewModel: VM

    private var activityLauncher: ActivityResultLauncher<Intent>? = null

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var arCallback: ((resultCode: Int, resultIntent: Intent?) -> Unit)? = null

    private var permissionAgree: (() -> Unit)? = null

    private var permissionRefuse: ((refusePermissions: ArrayList<String>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    /**
     *ViewModel绑定
     */
    private fun init() {
        viewModel = createViewModel(this, createViewModel())
        activityLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result != null) {
                arCallback?.let {
                    it(result.resultCode, result.data)
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()
        ) {
            val refusePermission: ArrayList<String> = ArrayList()
            it.keys.forEach { res ->
                if (it[res] == false) {
                    refusePermission.add(res)
                }
            }

            if (refusePermission.size > 0) {
                permissionRefuse?.let {
                    it(refusePermission)
                }
            } else {
                permissionAgree?.let {
                    it()
                }
            }
        }
    }

    /**
     * 创建ViewModel
     */
    abstract fun createViewModel(): VM

    /** 是否屏蔽返回键
     * @return
     */
    protected open fun isRefusedBackPress(): Boolean {
        return false
    }

    private fun createViewModel(owner: ViewModelStoreOwner, viewModel: VM): VM {
        return ViewModelProvider(owner).get(viewModel.javaClass)
    }

    fun startActivity(intent: Intent, arCallback: (resultCode: Int, resultIntent: Intent?) -> Unit) {
        this.arCallback = arCallback
        activityLauncher?.launch(intent)
    }

    fun requestPermission(permissions: Array<String>, agree: () -> Unit, refuse: (refusePermissions: ArrayList<String>) -> Unit) {
        this.permissionAgree = agree
        this.permissionRefuse = refuse
        var allAgree = true
        for (permission in permissions){
            if( ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED){
                allAgree=false
                break
            }
        }
        if (allAgree) {
            permissionAgree?.let {
                it()
            }
            return
        }
        permissionLauncher?.launch(permissions)
    }

    override fun handleMessage(msg: Message): Boolean {
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isRefusedBackPress() && keyCode == KeyEvent.KEYCODE_BACK) {  //欢迎页 按物理返回键不能关闭APP
            return true
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}