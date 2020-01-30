package com.mediapickerlib.base

import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import com.mediapickerlib.BR
import com.mediapickerlib.R

abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {

    open fun getContainerID(): Int {
        return 0
    }

    private var myBinding: ViewDataBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        myBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, layoutResID)
        myBinding?.setVariable(BR.click, this@BaseActivity)
        /*findViewById<Toolbar>(R.id.toolbar)?.apply {
            setSupportActionBar(this)
            setNavigationOnClickListener { onBackPressed() }
        }*/
        init()
        initView(myBinding!!)
    }

    abstract var layoutResID: Int
    abstract fun initView(mBinding: ViewDataBinding)

    protected lateinit var prefs: MyPreference

    private fun init() {
        prefs = MyPreference(this)
    }

    override fun onBackPressed() {
        if (!popFragment()) {
            super.onBackPressed()
        }
    }

    fun popFragment(): Boolean {
        var isPop = false
        try {
            if (supportFragmentManager.backStackEntryCount > 0) {
                val tag =
                    supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1)
                        .name
                val fragment = supportFragmentManager.findFragmentByTag(tag)
                if (fragment is BaseFragment && fragment.onBackPressed()) {
                    isPop = true
                } else if (fragment is BaseContainerFragment && fragment.onBackPressed()) {
                    isPop = true
                } else {
                    isPop = true
                    supportFragmentManager.popBackStack()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isPop
    }

    override fun onClick(v: View?) {}

    private var progressDialog: ProgressDialog? = null

    fun showProgress() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this@BaseActivity).apply {
                setCancelable(false)
                setMessage(getString(R.string.please_wait))
                isIndeterminate = true
                setProgressStyle(ProgressDialog.STYLE_SPINNER)
            }
        }
        if (progressDialog?.isShowing == false)
            progressDialog?.show()
    }

    fun hideProgress() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.hide()
            progressDialog?.cancel()
            progressDialog = null
        }
    }

    fun isProgressShowing(): Boolean = progressDialog?.isShowing ?: false

    fun showErrorMessage(message: String) {
        if (myBinding != null)
            Snackbar.make(myBinding!!.root, message, Snackbar.LENGTH_LONG).apply {
                view.setBackgroundColor(Color.RED)
                show()
            }
    }

    fun showMessage(message: String) {
        if (myBinding != null)
            Snackbar.make(myBinding!!.root, message, Snackbar.LENGTH_LONG).apply {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    /**
     * Check permission is enable or what?
     */
    fun hasPermission(permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                val result =
                    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
                if (!result) {
                    return false
                }
            }
        }
        return true
    }

    fun getPermission(permissions: Array<String>, reqCode: Int) {
        ActivityCompat.requestPermissions(this, permissions, reqCode)
    }

}