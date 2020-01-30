package com.mediapickerlib.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import com.mediapickerlib.R
import com.mediapickerlib.activity.PreviewAct
import com.mediapickerlib.adapter.MediaAdapter
import com.mediapickerlib.base.BaseFragment
import com.mediapickerlib.base.MediaType
import com.mediapickerlib.base.SelectMedia
import com.mediapickerlib.builder.Media.Companion.getBuilder
import com.mediapickerlib.databinding.FrgSelectMediaBinding
import com.mediapickerlib.fetcher.ImageFetcher
import com.mediapickerlib.fetcher.MediaFetcher
import com.mediapickerlib.fetcher.VideoFetcher
import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Constants
import com.mediapickerlib.util.Constants.EXTRA_MEDIA
import com.mediapickerlib.util.Constants.VIDEO_TRIMMER_REQUEST_CODE
import com.mediapickerlib.util.Utility
import com.mediapickerlib.video_trimmer_java.VideoTrimmerActivity
import java.util.*
import kotlin.collections.ArrayList

class SelectMediaFrg : BaseFragment(), MediaAdapter.OnAdapterEventListener {

    private lateinit var mBinding: FrgSelectMediaBinding
    private lateinit var mMediaAdapter: MediaAdapter
    private val REQUEST_OPEN_PREVIEW_SCREEN = 100

    private var mLastImageDate: Long = 0
    private var mLastVideoDate: Long = 0
    private var mLastMediaDate: Long = 0

    companion object {
        fun newInstance(): SelectMediaFrg {
            val fragment = SelectMediaFrg()
            val argBundle = Bundle()
            fragment.arguments = argBundle
            return fragment
        }
    }

    override fun getLayout(): Int = R.layout.frg_select_media

    override fun initView(mBinding: ViewDataBinding, savedInstanceState: Bundle?) {
        this.mBinding = mBinding as FrgSelectMediaBinding
        checkArgument()
    }

    override fun onMultiSelectionModeEnable(isSelectionMode: Boolean) {
        if (isSelectionMode) {
            mBinding.layUnselectedMode.visibility = View.GONE
            mBinding.laySelectedMode.visibility = View.VISIBLE
        } else {
            mBinding.layUnselectedMode.visibility = View.VISIBLE
            mBinding.laySelectedMode.visibility = View.GONE
        }
    }

    override fun onSingleMediaSelected() {
        openPreviewScreen()
    }

    private fun checkArgument() {
        setAdapter()

        /**
         * start scan media based on media type
         */
        when (getBuilder().mediaType) {
            MediaType.BOTH -> updateMedia()
            MediaType.IMAGE -> updateImages()
            MediaType.VIDEO -> updateVideos()
        }
    }

    private fun setAdapter() {
        mMediaAdapter = MediaAdapter(context!!, this)
        val layoutManager =
            GridLayoutManager(context, context?.resources?.getInteger(R.integer.span_count) ?: 4)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (mMediaAdapter.getItemViewType(position) == MediaAdapter.HEADER) {
                    context?.resources?.getInteger(R.integer.span_count) ?: 4
                } else 1
            }
        }
        mBinding.rvMediaList.layoutManager = layoutManager
        mBinding.rvMediaList.setHasFixedSize(true)
        mBinding.rvMediaList.setItemViewCacheSize(100)
        mBinding.rvMediaList.adapter = mMediaAdapter
    }

    private fun updateMedia() {
        val lastImageDateCur = Utility.getCursorOfLastImageDate(context)
        lastImageDateCur?.let {
            if (it.count == 0) return@let
            it.moveToNext()
            mLastImageDate =
                it.getLong(it.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN))
        }

        val lastVideoDateCur = Utility.getCursorOfLastVideoDate(context)
        lastVideoDateCur?.let {
            if (it.count == 0) return@let
            it.moveToNext()
            mLastVideoDate =
                it.getLong(it.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))
        }

        /**
         * if not found image or video then display no media found UI
         */
        if (mLastImageDate == 0L && mLastVideoDate == 0L) {
            setUI()
            return
        }

        mLastMediaDate =
            if (mLastImageDate <= mLastVideoDate) mLastImageDate else mLastVideoDate

        mMediaAdapter.clearList()

        val INSTANTLIST = ArrayList<Img>()

        INSTANTLIST.addAll(getData(Calendar.getInstance().time, Utility.getRecentDate()))
        INSTANTLIST.addAll(getData(Utility.getRecentDate(), Utility.getLastWeekDate()))
        INSTANTLIST.addAll(getData(Utility.getLastWeekDate(), Utility.getLastMonthDate()))

        object : MediaFetcher(context, mLastMediaDate) {
            override fun onPostExecute(imgs: ArrayList<Img>?) {
                super.onPostExecute(imgs)
                imgs?.let {
                    mMediaAdapter.addImageList(it)
                    setUI()
                }
            }
        }.execute()
        mMediaAdapter.addImageList(INSTANTLIST)
        setUI()
    }

    private fun updateImages() {
        mMediaAdapter.clearList()
        val INSTANTLIST = java.util.ArrayList<Img>()
        var header = ""
        var limit = 100
        val date: Int
        val data: Int
        val contentUrl: Int
        val size: Int
        var calendar: Calendar

        val cursorImage = Utility.getCursorImage(context)
        cursorImage?.let {
            if (it.count < 100) {
                limit = it.count
            }
            date = it.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            data = it.getColumnIndex(MediaStore.Images.Media.DATA)
            contentUrl = it.getColumnIndex(MediaStore.Images.Media._ID)
            size = it.getColumnIndex(MediaStore.Images.Media.SIZE)
            for (i in 0 until limit) {
                it.moveToNext()
                val path = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "" + it.getInt(contentUrl)
                )
                calendar = Calendar.getInstance()
                calendar.timeInMillis = it.getLong(date)
                val dateDifference = Utility.getDateDifference(context, calendar)
                if (!header.equals("" + dateDifference, ignoreCase = true)) {
                    header = "" + dateDifference
                    INSTANTLIST.add(
                        Img(
                            "" + dateDifference,
                            "",
                            "",
                            0,
                            false,
                            "",
                            calendar.time
                        )
                    )
                }
                INSTANTLIST.add(
                    Img(
                        "" + header,
                        "" + path,
                        it.getString(data),
                        it.getLong(size),
                        false,
                        "",
                        calendar.time
                    )
                )
            }
            it.close()
        }

        object : ImageFetcher(context) {
            override fun onPostExecute(imgs: java.util.ArrayList<Img>?) {
                super.onPostExecute(imgs)
                imgs?.let {
                    mMediaAdapter.addImageList(imgs)
                    setUI()
                }
            }
        }.execute()
        mMediaAdapter.addImageList(INSTANTLIST)
        setUI()
    }

    private fun updateVideos() {
        mMediaAdapter.clearList()
        val INSTANTLIST = java.util.ArrayList<Img>()
        var header = ""
        var limit = 100
        val date: Int
        val data: Int
        val contentUrl: Int
        val size: Int
        var calendar: Calendar

        val cursorVideo = Utility.getCursorVideo(context)
        cursorVideo?.let {
            if (it.count < 100) {
                limit = it.count
            }
            date = it.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            data = it.getColumnIndex(MediaStore.Images.Media.DATA)
            contentUrl = it.getColumnIndex(MediaStore.Images.Media._ID)
            size = it.getColumnIndex(MediaStore.Images.Media.SIZE)
            for (i in 0 until limit) {
                it.moveToNext()
                val path = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "" + it.getInt(contentUrl)
                )
                calendar = Calendar.getInstance()
                calendar.timeInMillis = it.getLong(date)
                val dateDifference = Utility.getDateDifference(context, calendar)
                if (!header.equals("" + dateDifference, ignoreCase = true)) {
                    header = "" + dateDifference
                    INSTANTLIST.add(
                        Img(
                            "" + dateDifference,
                            "",
                            "",
                            0,
                            false,
                            "",
                            calendar.time
                        )
                    )
                }
                INSTANTLIST.add(
                    Img(
                        "" + header,
                        "" + path,
                        it.getString(data),
                        it.getLong(size),
                        true,
                        "",
                        calendar.time
                    )
                )
            }
            it.close()
        }

        object : VideoFetcher(context) {
            override fun onPostExecute(imgs: ArrayList<Img>?) {
                super.onPostExecute(imgs)
                imgs?.let {
                    mMediaAdapter.addImageList(imgs)
                    setUI()
                }
            }
        }.execute()
        mMediaAdapter.addImageList(INSTANTLIST)
        setUI()
    }

    private fun getData(startDate: Date, endDate: Date): ArrayList<Img> {
        val TEMPLIST = ArrayList<Img>()
        /**
         * START get recent image
         */
        val cursorImage = Utility.getImageCursorByDate(context, startDate, endDate)
        val calendar = Calendar.getInstance()

        cursorImage?.let {
            val dateImage = it.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val dataImage = it.getColumnIndex(MediaStore.Images.Media.DATA)
            val contentUrlImage = it.getColumnIndex(MediaStore.Images.Media._ID)
            val sizeImage = it.getColumnIndex(MediaStore.Images.Media.SIZE)
            for (i in 0 until it.count) {
                it.moveToNext()
                val path = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "" + it.getInt(contentUrlImage)
                )
                calendar.timeInMillis = it.getLong(dateImage)
                val dateDifference = Utility.getDateDifference(context, calendar)
                TEMPLIST.add(
                    Img(
                        "" + dateDifference, "" + path, it.getString(dataImage),
                        it.getLong(sizeImage), false, "", calendar.time
                    )
                )
            }
            it.close()
        }

        val cursorVideo = Utility.getVideoCursorByDate(context, startDate, endDate)
        cursorVideo?.let {
            val dateVideo = it.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
            val dataVideo = it.getColumnIndex(MediaStore.Video.Media.DATA)
            val contentUrlVideo = it.getColumnIndex(MediaStore.Video.Media._ID)
            val sizeVideo = it.getColumnIndex(MediaStore.Video.Media.SIZE)

            for (i in 0 until it.count) {
                it.moveToNext()
                val path = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    "" + it.getInt(contentUrlVideo)
                )
                calendar.timeInMillis = it.getLong(dateVideo)
                val dateDifference = Utility.getDateDifference(context, calendar)

                TEMPLIST.add(
                    Img(
                        "" + dateDifference,
                        "" + path,
                        it.getString(dataVideo),
                        it.getLong(sizeVideo),
                        true,
                        "",
                        calendar.time
                    )
                )
            }
            it.close()
        }

        TEMPLIST.sort()

        /**
         * add header
         */
        if (TEMPLIST.size > 0) {
            calendar.timeInMillis = TEMPLIST[0].dateTaken.time + 1
            TEMPLIST.add(
                0, Img(
                    "" + Utility.getDateDifference(context, calendar), "", "",
                    0, false, "", calendar.time
                )
            )
        }
        /**
         * END get recent media
         */
        return TEMPLIST
    }

    private fun setUI() {
        mBinding.progressBar.visibility = View.GONE
        if (mMediaAdapter.getMediaList().isNullOrEmpty()) {
            mBinding.rvMediaList.visibility = View.GONE
            mBinding.layMediaNotFound.visibility = View.VISIBLE
        } else {
            mBinding.rvMediaList.visibility = View.VISIBLE
            mBinding.layMediaNotFound.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.btn_back -> {
                activity?.onBackPressed()
            }
            R.id.btn_cross -> {
                mMediaAdapter.deSelectMedia()
            }
            R.id.btn_ok -> {
                openPreviewScreen()
            }
        }
    }

    private fun openPreviewScreen() {


        openPreviewScreen(mMediaAdapter.getSelectedMedia())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OPEN_PREVIEW_SCREEN -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (getBuilder().selectMedia == SelectMedia.SINGLE) {
                        mMediaAdapter.clearSelectedMedia()
                    }
                }
            }
            else -> {

            }
        }
    }

    fun openPreviewScreen(mediaList: java.util.ArrayList<Img>) {
        val media = mediaList[0]
        if (media.isVideo) {
            val intent = Intent(
                activity!!,
                VideoTrimmerActivity::class.java
            )
            intent.putExtra(EXTRA_MEDIA, media.url)
            activity!!.startActivityForResult(intent, VIDEO_TRIMMER_REQUEST_CODE)
            return
        } else {
            val intent = Intent(context, PreviewAct::class.java)
            intent.putExtra(Constants.EXTRA_MEDIA_LIST, mediaList)
            startActivityForResult(intent, REQUEST_OPEN_PREVIEW_SCREEN)
        }
    }
}