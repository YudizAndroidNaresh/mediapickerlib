package com.mediapickerlib.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import androidx.databinding.ViewDataBinding
import com.mediapickerlib.R
import com.mediapickerlib.base.BaseFragment
import com.mediapickerlib.databinding.FrgVideoPreviewBinding
import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Constants


class VideoPreviewFrg : BaseFragment() {

    private lateinit var mBinding: FrgVideoPreviewBinding
    private lateinit var mImg: Img
    private val TAG = "VideoPreviewFrg"

    companion object {
        fun newInstance(img: Img): VideoPreviewFrg {
            val fragment = VideoPreviewFrg()
            val argBundle = Bundle()
            argBundle.putSerializable(Constants.EXTRA_IMG, img)
            fragment.arguments = argBundle
            return fragment
        }
    }

    override fun getLayout(): Int = R.layout.frg_video_preview

    override fun initView(mBinding: ViewDataBinding, savedInstanceState: Bundle?) {
        this.mBinding = mBinding as FrgVideoPreviewBinding
        initVideoView()
        checkArgument()
    }

    private fun checkArgument() {
        arguments?.let {
            if (it.containsKey(Constants.EXTRA_IMG)) {
                mImg = it.getSerializable(Constants.EXTRA_IMG) as Img
                mBinding.vvPreview.setVideoURI(Uri.parse(mImg.url))
            }
        }
    }

    private fun initVideoView() {
        val mediaController = MediaController(context)
        mediaController.setAnchorView(this.mBinding.vvPreview)

        mBinding.vvPreview.setMediaController(mediaController)
        mBinding.vvPreview.requestFocus()

        mBinding.vvPreview.setOnPreparedListener { mp ->
            mp.setOnVideoSizeChangedListener { mp, width, height ->
                mBinding.vvPreview.setMediaController(mediaController)
                mediaController.setAnchorView(mBinding.vvPreview)
            }
        }

        mBinding.vvPreview.setOnCompletionListener { mp ->

        }

        mBinding.vvPreview.setOnErrorListener { mp, what, extra ->
            Log.e(TAG, "$what - $extra")
            false
        }
    }

}