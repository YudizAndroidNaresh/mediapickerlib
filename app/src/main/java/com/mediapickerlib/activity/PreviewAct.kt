package com.mediapickerlib.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.ViewPager
import com.mediapickerlib.R
import com.mediapickerlib.adapter.CustomPagerAdapter
import com.mediapickerlib.adapter.SingleSelectionMediaAdapter
import com.mediapickerlib.base.BaseActivity
import com.mediapickerlib.builder.Media.Companion.getBuilder
import com.mediapickerlib.builder.Media.Companion.onMediaPick
import com.mediapickerlib.cropper.CropImage
import com.mediapickerlib.databinding.ActPreviewBinding
import com.mediapickerlib.fragment.ImagePreviewFrg
import com.mediapickerlib.fragment.VideoPreviewFrg
import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Constants

class PreviewAct : BaseActivity() {

    private lateinit var mBinding: ActPreviewBinding
    private var mMediaList: ArrayList<Img> = ArrayList()
    private lateinit var mPagerAdapter: CustomPagerAdapter

    override var layoutResID: Int = R.layout.act_preview

    override fun initView(mBinding: ViewDataBinding) {
        this.mBinding = mBinding as ActPreviewBinding
        setAdapter()
        checkIntent()
    }

    private fun setAdapter() {
        mBinding.rvMedia.adapter = SingleSelectionMediaAdapter(
            this,
            mMediaList,
            object : SingleSelectionMediaAdapter.OnSelectedMediaAdapterListener {
                override fun onMediaSelected(position: Int) {
                    setCurrentFragment(position)
                }
            })
    }

    private fun checkIntent() {
        intent?.let {
            if (it.hasExtra(Constants.EXTRA_MEDIA_LIST)) {
                mMediaList.addAll(it.getSerializableExtra(Constants.EXTRA_MEDIA_LIST) as ArrayList<Img>)
                setAllTabs()

                /**
                 * if media size only one then hide bottom list
                 */
                if (mMediaList.size <= 1) {
                    mBinding.rvMedia.visibility = View.GONE
                } else {
                    mBinding.rvMedia.visibility = View.VISIBLE
                }

                setBtnDeleteMediaVisibility()
            }
        }
    }

    private fun setAllTabs() {
        mPagerAdapter = CustomPagerAdapter(supportFragmentManager)
        mMediaList.forEach {
            if (it.isVideo) {
                mPagerAdapter.addFrag(VideoPreviewFrg.newInstance(it), getString(R.string.video))
            } else {
                mPagerAdapter.addFrag(ImagePreviewFrg.newInstance(it), getString(R.string.image))
            }
        }
        mBinding.viewPager.adapter = mPagerAdapter
        mBinding.viewPager.offscreenPageLimit = mMediaList.size - 1

        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                setCurrentFragment(position)
                /**
                 * if item not visible then scroll to visible
                 */
                mBinding.rvMedia.smoothScrollToPosition(position)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        /**
         * if first position are video then hide crop button
         */
        updateUI(0)
    }

    /**
     * if selected media list size 1 or less then 1 then hide delete button
     */
    private fun setBtnDeleteMediaVisibility() {
        if (mMediaList.size <= 1) {
            mBinding.btnDelete.visibility = View.GONE
        } else {
            mBinding.btnDelete.visibility = View.VISIBLE
        }
    }

    private fun setCurrentFragment(position: Int) {
        mBinding.viewPager.currentItem = position
        updateUI(position)
    }

    private fun removeMedia(position: Int) {
        mMediaList.removeAt(position)
        mBinding.rvMedia.adapter?.notifyItemRemoved(position)
        mPagerAdapter.remove(position)
        setBtnDeleteMediaVisibility()
    }

    private fun openCropScreen(uri: String) {
        getBuilder().cropImageBuilder?.start(this, Uri.parse(uri))
    }

    private fun updateUI(position: Int) {
        /**
         * {Media.builder.cropImageBuilder} null then not show crop button
         */
        if (getBuilder().cropImageBuilder == null || mMediaList.size == 0) {
            mBinding.btnCrop.visibility = View.GONE
            return
        }
        if (mMediaList[position].isVideo) {
            mBinding.btnCrop.visibility = View.GONE
        } else {
            mBinding.btnCrop.visibility = View.VISIBLE
        }
    }

    private fun setCoopedImage(uri: Uri) {
        mMediaList[mBinding.viewPager.currentItem].contentUrl = uri.toString()
        (mPagerAdapter.fragments[mBinding.viewPager.currentItem] as ImagePreviewFrg).refresh()
        mBinding.rvMedia.adapter?.notifyItemChanged(mBinding.viewPager.currentItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                setCoopedImage(result.uri)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.btn_back -> {
                onBackPressed()
            }
            R.id.btn_crop -> {
                val originalImgUrl =
                    (mPagerAdapter.fragments[mBinding.viewPager.currentItem] as ImagePreviewFrg).getOriginalUrl()
                openCropScreen(originalImgUrl)
            }
            R.id.btn_delete -> {
                removeMedia(mBinding.viewPager.currentItem)
            }
            R.id.btn_send -> {
                onMediaPick(mMediaList)
                MediaPickerAct.instant.finish()
                finish()
            }
        }
    }
}
