package com.mediapickerlib.fetcher

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore

import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Utility

import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date

open class MediaFetcher(private val context: Context?, private val mLastMediaDate: Long) :
    AsyncTask<Void, Void, ArrayList<Img>>() {
    private val mMediaList = ArrayList<Img>()
    private lateinit var startDate: Date
    private lateinit var endDate: Date

    override fun doInBackground(vararg params: Void): ArrayList<Img> {

        startDate = Utility.getLastMonthDate()
        val mCalender = Calendar.getInstance()
        mCalender.time = startDate

        while (mLastMediaDate <= mCalender.timeInMillis) {
            mCalender.add(Calendar.MONTH, -1)
            mCalender.set(
                mCalender.get(Calendar.YEAR),
                mCalender.get(Calendar.MONTH),
                mCalender.getActualMaximum(Calendar.DAY_OF_MONTH),
                mCalender.getActualMaximum(Calendar.HOUR_OF_DAY),
                mCalender.getActualMaximum(Calendar.MINUTE),
                mCalender.getActualMaximum(Calendar.SECOND)
            )
            mCalender.set(Calendar.MILLISECOND, mCalender.getActualMaximum(Calendar.MILLISECOND))

            endDate = mCalender.time

            mMediaList.addAll(getData(startDate, endDate))
            startDate = endDate
        }
        return mMediaList
    }

    private fun getData(startDate: Date, endDate: Date?): ArrayList<Img> {
        val TEMPLIST = ArrayList<Img>()
        /**
         * START get recent image
         */
        val cursorImage = Utility.getImageCursorByDate(context, startDate, endDate!!)
        var calendar = Calendar.getInstance()

        val dateImage = cursorImage?.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN) ?: -1
        cursorImage?.let {
            val dataImage = it.getColumnIndex(MediaStore.Images.Media.DATA)
            val contentUrlImage = it.getColumnIndex(MediaStore.Images.Media._ID)
            val sizeImage = it.getColumnIndex(MediaStore.Images.Media.SIZE)
            for (i in 0 until it.count) {
                it.moveToNext()
                val path = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "" + it.getInt(contentUrlImage)
                )
                calendar = Calendar.getInstance()
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
                calendar = Calendar.getInstance()
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
            //            calendar.setTimeInMillis(TEMPLIST.get(0).getDateTaken().getTime() + 1);
            TEMPLIST.add(
                0, Img(
                    "" + Utility.getDateDifference(context, calendar), "", "",
                    0, false, "", TEMPLIST[0].dateTaken
                )
            )
        }
        /**
         * END get recent media
         */
        return TEMPLIST
    }
}
