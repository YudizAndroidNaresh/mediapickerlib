package com.mediapickerlib.util

import android.provider.MediaStore

object Constants {
    const val SELECT_IMAGE = 100
    const val CAPTURE_IMAGE = 101

    /** Milliseconds used for UI animations */
    const val ANIMATION_FAST_MILLIS = 50L
    const val ANIMATION_SLOW_MILLIS = 100L

    const val EXTRA_MEDIA = "extra_media"
    const val EXTRA_IMG = "extra_img"
    const val EXTRA_LISTENER = "extra_listener"
    const val EXTRA_MEDIA_LIST = "extra_media_list"

    const val VIDEO_TRIMMER_REQUEST_CODE = 109

    const val INTENT_VIDEO_FILE ="video_file"


    /**
     * Image
     */
    var URI_IMAGE = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    var PROJECTION_IMAGE = arrayOf(
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.DATE_TAKEN,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.SIZE
    )
    var ORDERBY_IMAGE_DESC = MediaStore.Images.Media.DATE_TAKEN + " DESC"


    /**
     * Video
     */
    var URI_VIDEO = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    var PROJECTION_VIDEO = arrayOf(
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.DATE_TAKEN,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.SIZE
    )
    var ORDERBY_VIDEO_DESC = MediaStore.Video.Media.DATE_TAKEN + " DESC"


    /**
     * Media (Image & Video)
     */
    var SELECTION_MEDIA =
        MediaStore.Images.Media.DATE_TAKEN + "<=? and " + MediaStore.Images.Media.DATE_TAKEN + ">=?"

}
