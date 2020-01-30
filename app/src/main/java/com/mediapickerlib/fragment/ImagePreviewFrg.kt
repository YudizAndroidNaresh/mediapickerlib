package com.mediapickerlib.fragment

import android.net.Uri
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.mediapickerlib.R
import com.mediapickerlib.base.BaseFragment
import com.mediapickerlib.databinding.FrgImagePreviewBinding
import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Constants

class ImagePreviewFrg : BaseFragment() {

    private lateinit var mBinding: FrgImagePreviewBinding
    private lateinit var mImg: Img
    private var mImagePath: String = ""

    companion object {
        fun newInstance(img: Img): ImagePreviewFrg {
            val fragment = ImagePreviewFrg()
            val argBundle = Bundle()
            argBundle.putSerializable(Constants.EXTRA_IMG, img)
            fragment.arguments = argBundle
            return fragment
        }
    }

    override fun getLayout(): Int = R.layout.frg_image_preview

    override fun initView(mBinding: ViewDataBinding, savedInstanceState: Bundle?) {
        this.mBinding = mBinding as FrgImagePreviewBinding
        checkArgument()
    }

    private fun checkArgument() {
        arguments?.let {
            if (it.containsKey(Constants.EXTRA_IMG)) {
                mImg = it.getSerializable(Constants.EXTRA_IMG) as Img
                mImagePath = mImg.contentUrl
                mBinding.media = mImg
            }
        }
    }

    fun getOriginalUrl() = mImagePath

    fun refresh() {
        mBinding.media = mImg
    }
}