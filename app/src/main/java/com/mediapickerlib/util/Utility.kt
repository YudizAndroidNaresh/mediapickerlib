package com.mediapickerlib.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.mediapickerlib.R
import java.text.SimpleDateFormat
import java.util.*

class Utility {
    companion object{
        fun getCursorOfLastImageDate(context: Context?): Cursor? {
            return context?.contentResolver?.query(
                Constants.URI_IMAGE,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN),
                null, null,
                MediaStore.Images.Media.DATE_TAKEN + " ASC limit 1"
            )
        }

        fun getCursorOfLastVideoDate(context: Context?): Cursor? {
            return context?.contentResolver?.query(
                Constants.URI_VIDEO,
                arrayOf(MediaStore.Video.Media.DATE_TAKEN),
                null, null,
                MediaStore.Video.Media.DATE_TAKEN + " ASC limit 1"
            )
        }

        fun getImageCursorByDate(context: Context?, date1: Date, date2: Date): Cursor? {

            return context?.contentResolver?.query(
                Constants.URI_IMAGE, null, Constants.SELECTION_MEDIA,
                arrayOf("" + date1.time, "" + date2.time),
                MediaStore.Images.Media.DATE_TAKEN + " DESC"
            )
        }

        fun getVideoCursorByDate(context: Context?, date1: Date, date2: Date): Cursor? {

            return context?.contentResolver?.query(
                Constants.URI_VIDEO, null, Constants.SELECTION_MEDIA,
                arrayOf("" + date1.time, "" + date2.time),
                MediaStore.Video.Media.DATE_TAKEN + " DESC"
            )
        }

        fun getCursorImage(context: Context?): Cursor? {
            return context?.contentResolver?.query(
                Constants.URI_IMAGE,
                Constants.PROJECTION_IMAGE,
                null,
                null,
                Constants.ORDERBY_IMAGE_DESC
            )
        }

        fun getCursorVideo(context: Context?): Cursor? {
            return context?.contentResolver?.query(
                Constants.URI_VIDEO,
                Constants.PROJECTION_VIDEO,
                null,
                null,
                Constants.ORDERBY_VIDEO_DESC
            )
        }

        fun getLastMonthDate(): Date {
            val lastMonth = Calendar.getInstance()
            lastMonth.timeZone = TimeZone.getTimeZone("UTC")
            lastMonth.add(Calendar.DAY_OF_MONTH, -30)
            return lastMonth.time
        }

        fun getLastWeekDate(): Date {
            val lastWeek = Calendar.getInstance()
            lastWeek.timeZone = TimeZone.getTimeZone("UTC")
            lastWeek.add(Calendar.DAY_OF_MONTH, -7)
            return lastWeek.time
        }

        fun getRecentDate(): Date {
            val recent = Calendar.getInstance()
            recent.timeZone = TimeZone.getTimeZone("UTC")
            recent.add(Calendar.DAY_OF_MONTH, -2)
            return recent.time
        }

        @SuppressLint("SimpleDateFormat")
        fun getDateDifference(context: Context?, calendar: Calendar): String {
            val d = calendar.time
            val timeZone = TimeZone.getTimeZone("UTC")
            val lastMonth = Calendar.getInstance()
            val lastWeek = Calendar.getInstance()
            val recent = Calendar.getInstance()
            lastMonth.timeZone = timeZone
            lastWeek.timeZone = timeZone
            recent.timeZone = timeZone
            lastMonth.add(Calendar.DAY_OF_MONTH, -30)
            lastWeek.add(Calendar.DAY_OF_MONTH, -7)
            recent.add(Calendar.DAY_OF_MONTH, -2)

            return if (calendar.before(lastMonth)) {
                SimpleDateFormat("MMM-yy").format(d)
            } else if (calendar.after(lastMonth) && calendar.before(lastWeek)) {
                context?.resources?.getString(R.string.pix_last_month) ?: ""
            } else if (calendar.after(lastWeek) && calendar.before(recent)) {
                context?.resources?.getString(R.string.pix_last_week) ?: ""
            } else {
                context?.resources?.getString(R.string.pix_recent) ?: ""
            }
        }
    }
}