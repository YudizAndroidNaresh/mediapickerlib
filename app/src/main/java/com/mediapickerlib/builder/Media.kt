package com.mediapickerlib.builder

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mediapickerlib.activity.MediaPickerAct
import com.mediapickerlib.activity.PreviewAct
import com.mediapickerlib.adapter.MediaAdapter
import com.mediapickerlib.base.MediaType
import com.mediapickerlib.base.SelectMedia
import com.mediapickerlib.base.SourceType
import com.mediapickerlib.camera2.fragment.Camera2Fragment
import com.mediapickerlib.cropper.CropImage
import com.mediapickerlib.fragment.CameraFrg
import com.mediapickerlib.fragment.SelectMediaFrg
import com.mediapickerlib.listener.OnMediaPickerListener
import com.mediapickerlib.modals.Img
import com.mediapickerlib.video_trimmer_java.VideoTrimmerActivity

class Media {

    companion object {
        private lateinit var mListener: OnMediaPickerListener
        private lateinit var builder: Builder

        fun activity(): Builder {
            builder = Builder()
            return builder
        }

        fun setListener(listener: OnMediaPickerListener) {
            mListener = listener
        }

        fun VideoTrimmerActivity.onMediaPick(mediaList: ArrayList<Img>) {
            if (::mListener.isInitialized)
                mListener.onMediaPick(mediaList)
        }


        fun PreviewAct.onMediaPick(mediaList: ArrayList<Img>) {
            if (::mListener.isInitialized)
                mListener.onMediaPick(mediaList)
        }

        fun MediaAdapter.getBuilder() = builder
        fun PreviewAct.getBuilder() = builder
        fun MediaPickerAct.getBuilder() = builder
        fun SelectMediaFrg.getBuilder() = builder
        fun CameraFrg.getBuilder() = builder
        fun Camera2Fragment.getBuilder() = builder
    }

    class Builder() : Parcelable {

        companion object CREATOR : Parcelable.Creator<Builder> {
            override fun createFromParcel(parcel: Parcel): Builder {
                return Builder(parcel)
            }

            override fun newArray(size: Int): Array<Builder?> {
                return arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this() {
            val sourceTypeTmp = parcel.readInt()
            sourceType = SourceType.values()[sourceTypeTmp]
            val mediaTypeTmp = parcel.readInt()
            mediaType = MediaType.values()[mediaTypeTmp]
            val selectMediaTmp = parcel.readInt()
            selectMedia = SelectMedia.values()[selectMediaTmp]
            selectMediaCount = parcel.readInt()
            selectMediaMaxSize = parcel.readLong()
            cropImageBuilder =
                parcel.readParcelable(CropImage.ActivityBuilder::class.java.classLoader)
        }

        override fun writeToParcel(parcel: Parcel?, flags: Int) {
            parcel?.writeInt(sourceType.ordinal)
            parcel?.writeInt(mediaType.ordinal)
            parcel?.writeInt(selectMedia.ordinal)
            parcel?.writeInt(selectMediaCount)
            parcel?.writeLong(selectMediaMaxSize)
            parcel?.writeParcelable(cropImageBuilder, flags)
        }

        override fun describeContents(): Int {
            return 0
        }

        var sourceType: SourceType = SourceType.BOTH
        var mediaType: MediaType = MediaType.BOTH
        var selectMedia: SelectMedia = SelectMedia.SINGLE
        var selectMediaCount: Int = -1
        var selectMediaMaxSize: Long = -1
        var cropImageBuilder: CropImage.ActivityBuilder? = null


        fun setSourceType(sourceType: SourceType): Builder = apply { this.sourceType = sourceType }

        fun setMediaType(mediaType: MediaType): Builder = apply { this.mediaType = mediaType }

        fun setSelectMedia(selectMedia: SelectMedia): Builder =
            apply { this.selectMedia = selectMedia }

        fun setSelectMediaCount(selectMediaCount: Int): Builder =
            apply { this.selectMediaCount = selectMediaCount }

        fun setSelectMediaMaxSize(selectMediaMaxSize: Long): Builder =
            apply { this.selectMediaMaxSize = selectMediaMaxSize }

        fun setCropImageBuilder(cropImageBuilder: CropImage.ActivityBuilder?): Builder =
            apply { this.cropImageBuilder = cropImageBuilder }

        fun start(activity: AppCompatActivity) {
            val intent = Intent(activity, MediaPickerAct::class.java)
            activity.startActivityForResult(intent,109)
        }

        fun start(fragment: Fragment) {
            val intent = Intent(fragment.context, MediaPickerAct::class.java)
            fragment.startActivityForResult(intent,109)
        }
    }
}