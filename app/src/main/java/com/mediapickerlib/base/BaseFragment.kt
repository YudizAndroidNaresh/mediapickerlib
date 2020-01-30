package com.mediapickerlib.base

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mediapickerlib.BR
import com.mediapickerlib.R

abstract class BaseFragment : Fragment(), View.OnClickListener {

    protected var prefs: MyPreference? = null
    abstract fun initView(mBinding: ViewDataBinding, savedInstanceState: Bundle?)
    private var myBinding: ViewDataBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        prefs = MyPreference(activity!!)
        myBinding = DataBindingUtil.inflate(inflater, getLayout(), container, false)
        return myBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*view.findViewById<Toolbar>(R.id.toolbar)?.apply { setNavigationOnClickListener { activity?.onBackPressed() } }
        view.findViewById<Toolbar>(com.partaille.R.id.toolbar)?.apply {
            (activity as AppCompatActivity).setSupportActionBar(this)
            setNavigationOnClickListener { activity?.onBackPressed() }
        }*/
        myBinding?.setVariable(BR.click, this@BaseFragment)
        view.isClickable = true
        initView(myBinding!!, savedInstanceState)
    }

    abstract fun getLayout(): Int

    /**
     * onBackPressed() override in fragment for back button handling data
     */
    open fun onBackPressed(): Boolean {
        return false
    }

    override fun onClick(v: View?) {}

    private var progressDialog: ProgressDialog? = null

    fun showProgress() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(context).apply {
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
        try {
            if (progressDialog?.isShowing == true) {
                progressDialog?.hide()
                progressDialog?.cancel()
                progressDialog = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }
}