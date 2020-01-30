package com.mediapickerlib.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mediapickerlib.R
import com.mediapickerlib.base.BaseActivity
import com.mediapickerlib.base.MediaType
import com.mediapickerlib.base.SourceType
import com.mediapickerlib.base.replaceFragment
import com.mediapickerlib.builder.Media.Companion.getBuilder
import com.mediapickerlib.camera2.fragment.Camera2Fragment
import com.mediapickerlib.fragment.SelectMediaFrg
import com.mediapickerlib.util.Constants
import com.mediapickerlib.util.Constants.VIDEO_TRIMMER_REQUEST_CODE
import kotlinx.android.synthetic.main.act_media_picker.*

class MediaPickerAct : BaseActivity() {

    private val TAG = "MediaPickerAct"

    companion object {
        lateinit var instant: MediaPickerAct
    }

    val CAMERA_PERMISSIONS_WITH_AUDIO = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO
    )
    val CAMERA_PERMISSIONS_WITHOUT_AUDIO = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val STORAGE_PERMISSION = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val REQUEST_CAMERA_PERMISSION_WITH_AUDIO = 101
    val REQUEST_CAMERA_PERMISSION_WITHOUT_AUDIO = 102
    val REQUEST_STORAGE_PERMISSION = 103
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override var layoutResID: Int = R.layout.act_media_picker

    override fun getContainerID(): Int = R.id.root_layout

    override fun initView(mBinding: ViewDataBinding) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.navigationBarColor = resources.getColor(R.color.colorPrimary)
            window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        instant = this@MediaPickerAct

        startMediaPicker()
    }

    private fun startMediaPicker() {
        if (getBuilder().sourceType == SourceType.CAMERA) {
            if (getBuilder().mediaType == MediaType.IMAGE){
                checkPermission(
                    CAMERA_PERMISSIONS_WITHOUT_AUDIO,
                    REQUEST_CAMERA_PERMISSION_WITHOUT_AUDIO
                )
            } else if (getBuilder().mediaType == MediaType.VIDEO || getBuilder().mediaType == MediaType.BOTH){
                checkPermission(CAMERA_PERMISSIONS_WITH_AUDIO, REQUEST_CAMERA_PERMISSION_WITH_AUDIO)
            }

        } else if (getBuilder().sourceType == SourceType.GALLERY){
            checkPermission(STORAGE_PERMISSION, REQUEST_STORAGE_PERMISSION)
        } else {
            openSourceTypeDialog()
        }
    }

    private fun checkPermission(permissions: Array<String>, reqCode: Int) {
        if (hasPermission(permissions)) {
            when (reqCode) {
                REQUEST_CAMERA_PERMISSION_WITHOUT_AUDIO, REQUEST_CAMERA_PERMISSION_WITH_AUDIO -> {
                    openCaptureMediaFrg()
                }
                REQUEST_STORAGE_PERMISSION -> {
                    openSelectMediaFrg()
                }
            }
        } else {
            getPermission(permissions, reqCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CAMERA_PERMISSION_WITH_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                openCaptureMediaFrg()
            }
        }

        if (requestCode == REQUEST_CAMERA_PERMISSION_WITHOUT_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCaptureMediaFrg()
            }
        }

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openSelectMediaFrg()
            }
        }
    }

    private fun openSourceTypeDialog() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_COLLAPSED) {
                    finish()
                    overridePendingTransition(0, android.R.anim.fade_out)
                }
            }
        })
    }

    private fun openCaptureMediaFrg() {
        //replaceFragment(CameraFrg.newInstance())

        replaceFragment(Camera2Fragment.newInstance())
    }

     fun openSelectMediaFrg() {
        replaceFragment(SelectMediaFrg.newInstance(),SelectMediaFrg::class.java.simpleName)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v) {
            btn_camera -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                Handler().postDelayed({
                    if (getBuilder().mediaType == MediaType.IMAGE) {
                        checkPermission(
                            CAMERA_PERMISSIONS_WITHOUT_AUDIO,
                            REQUEST_CAMERA_PERMISSION_WITHOUT_AUDIO
                        )
                    } else if (getBuilder().mediaType == MediaType.VIDEO || getBuilder().mediaType == MediaType.BOTH) {
                        checkPermission(
                            CAMERA_PERMISSIONS_WITH_AUDIO,
                            REQUEST_CAMERA_PERMISSION_WITH_AUDIO
                        )
                    }
                }, 300)
            }

            btn_gallery -> {
                Handler().postDelayed({
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    checkPermission(STORAGE_PERMISSION, REQUEST_STORAGE_PERMISSION)
                }, 300)
            }

            root_layout -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    override fun onBackPressed() {
        if (::bottomSheetBehavior.isInitialized)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        super.onBackPressed()
        overridePendingTransition(0, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== VIDEO_TRIMMER_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }
}
