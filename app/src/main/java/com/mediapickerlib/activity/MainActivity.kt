package com.mediapickerlib.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.mediapickerlib.R
import com.mediapickerlib.base.BaseActivity
import com.mediapickerlib.base.MediaType
import com.mediapickerlib.base.SelectMedia
import com.mediapickerlib.base.SourceType
import com.mediapickerlib.builder.Media
import com.mediapickerlib.cropper.CropImage
import com.mediapickerlib.cropper.CropImageView
import com.mediapickerlib.databinding.ActivityMainBinding
import com.mediapickerlib.listener.OnMediaPickerListener
import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Constants
import com.mediapickerlib.util.Constants.INTENT_VIDEO_FILE
import com.mediapickerlib.util.Constants.VIDEO_TRIMMER_REQUEST_CODE

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override var layoutResID: Int = R.layout.activity_main

    override fun initView(mBinding: ViewDataBinding) {
        this.mBinding = mBinding as ActivityMainBinding
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        var mSourceType = SourceType.BOTH
        var mSelectMedia = SelectMedia.SINGLE
        var mMediaType = MediaType.BOTH

        when (v?.id) {

            R.id.btn_open_media_picker -> {
                mSourceType = when (mBinding.spSourceType.selectedItemPosition) {
                    0 -> SourceType.CAMERA
                    1 -> SourceType.GALLERY
                    2 -> SourceType.BOTH
                    else -> SourceType.BOTH
                }

                mMediaType = when (mBinding.spMediaType.selectedItemPosition) {
                    0 -> MediaType.IMAGE
                    1 -> MediaType.VIDEO
                    2 -> MediaType.BOTH
                    else -> MediaType.BOTH
                }

                mSelectMedia = when (mBinding.spSelectionType.selectedItemPosition) {
                    0 -> SelectMedia.SINGLE
                    1 -> SelectMedia.MULTIPLE
                    else -> SelectMedia.MULTIPLE
                }

                val cropBuilder = CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setActivityTitle("Crop Image")
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setCropMenuCropButtonTitle("Done")
                    .setRequestedSize(400, 400)

                Media.activity()
                    .setSourceType(mSourceType)
                    .setSelectMedia(mSelectMedia)
                    .setMediaType(mMediaType)
                    .setCropImageBuilder(if (mBinding.cbIsCropImage.isChecked) cropBuilder else null)
                    .setSelectMediaCount(if (mBinding.etMaxSelectionCount.text?.isBlank() == true) -1 else mBinding.etMaxSelectionCount.text.toString().toInt())
                    .setSelectMediaMaxSize(if (mBinding.etMaxSize.text?.isBlank() == true) -1 else mBinding.etMaxSize.text.toString().toLong())
                    .start(this)

                Media.setListener(object : OnMediaPickerListener {
                    override fun onMediaPick(mediaList: ArrayList<Img>) {
                        mBinding.tvResult.text = ""
                        for (img in mediaList) {
                            mBinding.tvResult.text =
                                "${mBinding.tvResult.text} ${img.contentUrl}\n\n"
                        }
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== VIDEO_TRIMMER_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this,data!!.getStringExtra(INTENT_VIDEO_FILE), Toast.LENGTH_LONG).show()
            }
        }
    }

}
