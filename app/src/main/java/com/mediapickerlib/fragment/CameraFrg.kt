package com.mediapickerlib.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.mediapickerlib.R
import com.mediapickerlib.activity.PreviewAct
import com.mediapickerlib.adapter.MultiSelectionMediaAdapter
import com.mediapickerlib.adapter.SingleSelectionMediaAdapter
import com.mediapickerlib.base.*
import com.mediapickerlib.builder.Media.Companion.getBuilder
import com.mediapickerlib.databinding.FrgCameraBinding
import com.mediapickerlib.modals.Img
import com.mediapickerlib.util.Constants
import com.mediapickerlib.util.Constants.ANIMATION_FAST_MILLIS
import com.mediapickerlib.util.Constants.ANIMATION_SLOW_MILLIS
import com.mediapickerlib.video_trimmer_java.VideoTrimmerActivity
import java.io.File
import java.text.DecimalFormat
import java.util.*

class CameraFrg : BaseFragment() {

    private lateinit var mBinding: FrgCameraBinding

    private lateinit var previewConfig: PreviewConfig
    private lateinit var preview: Preview

    private lateinit var imageAnalysisConfig: ImageAnalysisConfig
    private lateinit var imageAnalysis: ImageAnalysis

    private lateinit var imageCaptureConfig: ImageCaptureConfig
    private lateinit var imageCapture: ImageCapture

    private lateinit var videoCaptureConfig: VideoCaptureConfig
    private lateinit var videoCapture: VideoCapture

    private var lensFacing = CameraX.LensFacing.BACK
    private var isBackLens = true
    private var isImageCapture = true

    private lateinit var mAdapter: MultiSelectionMediaAdapter
    private val REQUEST_OPEN_PREVIEW_SCREEN = 100

    companion object {
        fun newInstance(): CameraFrg {
            val fragment = CameraFrg()
            val argBundle = Bundle()
            fragment.arguments = argBundle
            return fragment
        }
    }

    override fun getLayout(): Int = R.layout.frg_camera

    override fun initView(mBinding: ViewDataBinding, savedInstanceState: Bundle?) {
        this.mBinding = mBinding as FrgCameraBinding

        setAdapter()

        /**
         * set isBackLens variable to binding for back or front camera lens
         */
        this.mBinding.isBackLens = isBackLens
        /**
         * set isImageCapture variable to binding for image capture or video record
         */
        this.mBinding.isImageCapture = isImageCapture

        /**
         * if SourceType only captureImage then hide switchMode button
         */
        when (getBuilder().mediaType) {
            MediaType.IMAGE -> {
                isImageCapture = true
                this.mBinding.btnSwitchMode.visibility = View.GONE
            }
            MediaType.VIDEO -> {
                isImageCapture = false
                this.mBinding.btnSwitchMode.visibility = View.GONE
            }
            MediaType.BOTH -> {
                this.mBinding.btnSwitchMode.visibility = View.VISIBLE
            }
        }

        /**
         * start camera
         */
        this.mBinding.viewFinder.post { startCamera() }


        /**
         * addOnLayoutChangeListener on TextureView for update preview each frame
         */
        this.mBinding.viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    /**
     * setAdapter for display capture image or record video list and user also select or deselect capture media
     */
    private fun setAdapter() {
        mAdapter = MultiSelectionMediaAdapter(
            context!!,
            object : SingleSelectionMediaAdapter.OnSelectedMediaAdapterListener {
                override fun onMediaSelected(position: Int) {
                    uiUpdate()
                }
            })
        mBinding.rvMedia.adapter = mAdapter
    }

    private fun startCamera() {

        /**
         * unbind All for change cameraMode to refresh
         */
        CameraX.unbindAll()

        /**
         * Find center of view finder
         */
        val centerX = mBinding.viewFinder.width / 2f
        val centerY = mBinding.viewFinder.height / 2f
        val screenAspectRatio = Rational(centerX.toInt(), centerY.toInt())

        /**
         * UseCase 1 : Preview
         * Step 1 : Create PreviewConfig object
         */
        previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(screenAspectRatio)
//            setTargetResolution(Size(640, 640))
            setTargetResolution(Size(1280, 720))
            setLensFacing(lensFacing)
        }.build()

        // Step 2 : Create Preview object
        preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = mBinding.viewFinder.parent as ViewGroup
            parent.removeView(mBinding.viewFinder)
            parent.addView(mBinding.viewFinder, 0)

            mBinding.viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        /**
         * if (isImageCapture == true) only bind image related use case
         * else bind video related use cases
         */
        if (isImageCapture) {
            /**
             * UseCase 2 : Image Capture
             * Step 1 : Create ImageCaptureConfig object
             */
            imageAnalysisConfig = ImageAnalysisConfig.Builder()
                .setTargetResolution(Size(1280, 720))
                .setTargetAspectRatio(screenAspectRatio)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setLensFacing(lensFacing)
                .build()
            imageAnalysis = ImageAnalysis(imageAnalysisConfig)

            imageAnalysis.setAnalyzer { image: ImageProxy, rotationDegrees: Int ->
                // write your code here.
            }


            /**
             * UseCase 3 : Image Capture
             * Step 1 : Create ImageCaptureConfig object
             */
            imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    setTargetAspectRatio(screenAspectRatio)
                    setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                    setLensFacing(lensFacing)
                }.build()

            // Step 2 : Create ImageCapture object
            imageCapture = ImageCapture(imageCaptureConfig)

            /**
             * Bind all user cases to activity or fragment lifeCycle
             */
            CameraX.bindToLifecycle(
                this as LifecycleOwner,
                preview,
                imageAnalysis,
                imageCapture
            )
        } else {
            /**
             * UseCase 3 : Video Capture
             * Step 1 : Create VideoCaptureConfig object
             */
            videoCaptureConfig = VideoCaptureConfig.Builder().apply {
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(mBinding.viewFinder.display.rotation)
                setLensFacing(lensFacing)
            }.build()
            videoCapture = VideoCapture(videoCaptureConfig)

            /**
             * Bind all user cases to activity or fragment lifeCycle
             */
            CameraX.bindToLifecycle(
                this as LifecycleOwner,
                preview,
                videoCapture
            )
        }
    }

    /**
     * update capture image or video frame and display on TextureView for preview purpose
     */
    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = mBinding.viewFinder.width / 2f
        val centerY = mBinding.viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (mBinding.viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        mBinding.viewFinder.setTransform(matrix)
    }

    /**
     * call captureImage onClick capture image button
     */
    private fun captureImage() {
//        activity?.externalMediaDirs?.first()
        val path =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Camera"
        val file = File(path, "${System.currentTimeMillis()}.jpg")
        imageCapture.takePicture(file,
            object : ImageCapture.OnImageSavedListener {

                override fun onImageSaved(file: File) {
                    val imagePath = file.absolutePath
                    val uri = Uri.fromFile(file)
                    val msg = "Photo capture succeeded: ${imagePath}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d("CameraXApp", msg)
                    activity?.runOnUiThread {
                        val image = Img(
                            "",
                            uri.toString(),
                            uri.toString(),
                            file.length(),
                            false,
                            "",
                            Date()
                        )
                        mAdapter.addMedia(image)
                        addMediaToGallery(context, imagePath, image.dateTaken.time, image.isVideo)


                    }
                }

                override fun onError(
                    error: ImageCapture.UseCaseError,
                    message: String,
                    exc: Throwable?
                ) {
                    val msg = "Photo capture failed: $message"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.e("CameraXApp", msg)
                    exc?.printStackTrace()
                }
            })

        // We can only change the foreground Drawable using API level 23+ API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Display flash animation to indicate that photo was captured
            mBinding.rootLayout.postDelayed({
                mBinding.rootLayout.foreground = ColorDrawable(Color.WHITE)
                mBinding.rootLayout.postDelayed(
                    { mBinding.rootLayout.foreground = null }, ANIMATION_FAST_MILLIS
                )
            }, ANIMATION_SLOW_MILLIS)
        }
    }

    /**
     * call captureImage onClick record video button
     */

    private fun recordVideo() {
        /**
         * if (capture selected == true) stop recording video
         * else start recording video
         */
        if (mBinding.btnCapture.isSelected) {
            updateUI()
            videoCapture.stopRecording()
            Log.i(tag, "Video File stopped")
        } else {
            updateUI()
            val path =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Camera"
            val file = File(path, "${System.currentTimeMillis()}.mp4")

            videoCapture.startRecording(file, object : VideoCapture.OnVideoSavedListener {

                override fun onVideoSaved(file: File) {
                    val videoPath = file.absolutePath
                    val uri = Uri.fromFile(file)
                    val msg = "Video record succeeded: ${videoPath}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d("CameraXApp", msg)

                    /**
                     * if Builder media size -1 then not check condition
                     * check selected media size is not more {Builder media size}
                     */
                    if (getBuilder().selectMediaMaxSize != -1L && getBuilder().selectMediaMaxSize < file.length()) {
                        Toast.makeText(context, getMediaSizeExitsMessage(), Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                    activity?.runOnUiThread {
                        val video = Img(
                            "",
                            uri.toString(),
                            uri.toString(),
                            file.length(),
                            true,
                            "",
                            Date()
                        )
                        mAdapter.addMedia(video)
                        addMediaToGallery(context, videoPath, video.dateTaken.time, video.isVideo)


                        val intent =  Intent(activity!!,VideoTrimmerActivity::class.java)
                        intent.putExtra("EXTRA_PATH", videoPath)
                        activity!!.startActivity(intent)

                    }
                }

                override fun onError(
                    error: VideoCapture.UseCaseError?,
                    message: String?,
                    exc: Throwable?
                ) {
                    val msg = "Video record failed: $message"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.e("CameraXApp", msg)
                    exc?.printStackTrace()
                }
            })
        }
    }

    private fun uiUpdate() {
        if (getBuilder().selectMedia == SelectMedia.SINGLE) {
            openPreviewScreen()
            return
        }
        if (mAdapter.getCaptureMedia().size != 0) {
            mBinding.rvMedia.visibility = View.VISIBLE
        } else {
            mBinding.rvMedia.visibility = View.GONE
        }

        if (mAdapter.getSelectedMedia().size != 0) {
            mBinding.btnSend.show()
        } else {
            mBinding.btnSend.hide()
        }
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        when (v?.id) {
            R.id.btn_back -> {
                onBackPressed()
            }
            R.id.btn_send -> {
                openPreviewScreen()
            }
            R.id.btn_switch_mode -> {
                switchCameraMode()
            }
            R.id.btn_switch_camera -> {
                switchCameraLens()
            }
            R.id.btn_capture -> {
                /**
                 * if Builder media count -1 then not check condition
                 * check selected media count is not more than {Builder media count}
                 */
                if (getBuilder().selectMediaCount != -1 && getBuilder().selectMediaCount == mAdapter.getSelectedMedia().size) {
                    Toast.makeText(context, getSelectCountExitsMessage(), Toast.LENGTH_SHORT).show()
                    return
                }

                if (isImageCapture) {
                    captureImage()
                } else {
                    recordVideo()
                }
            }
        }
    }

    private fun switchCameraMode() {
        isImageCapture = !isImageCapture
        mBinding.isImageCapture = isImageCapture
        startCamera()
    }

    private fun switchCameraLens() {
        lensFacing = if (isBackLens) CameraX.LensFacing.FRONT else CameraX.LensFacing.BACK
        try {
            // Only bind use cases if we can query a camera with this orientation
            CameraX.getCameraWithLensFacing(lensFacing)

            /**
             * restart camera :
             * Unbind all use cases and bind them again with the new lens facing configuration
             */
            startCamera()

            //Change UI
            isBackLens = !isBackLens
            mBinding.isBackLens = isBackLens
        } catch (exc: Exception) {
            // Do nothing
        }
    }

    private fun updateUI() {
        if (mBinding.btnCapture.isSelected) {
            mBinding.btnCapture.isSelected = false

            mBinding.btnSwitchCamera.visibility = View.VISIBLE
            /**
             * if SourceType only captureImage then hide switchMode button
             */
            when (getBuilder().mediaType) {
                MediaType.IMAGE -> {
                    isImageCapture = true
                    this.mBinding.btnSwitchMode.visibility = View.GONE
                }
                MediaType.VIDEO -> {
                    isImageCapture = false
                    this.mBinding.btnSwitchMode.visibility = View.GONE
                }
                MediaType.BOTH -> {
                    this.mBinding.btnSwitchMode.visibility = View.VISIBLE
                }
            }
            return
        }

        mBinding.btnCapture.isSelected = true
        mBinding.btnSwitchCamera.visibility = View.GONE
        mBinding.btnSwitchMode.visibility = View.GONE
    }

    private fun getDisplayMediaType(): String {
        return when (getBuilder().mediaType) {
            MediaType.IMAGE -> context?.getString(R.string.image) ?: ""
            MediaType.VIDEO -> context?.getString(R.string.video) ?: ""
            MediaType.BOTH -> context?.getString(R.string.media) ?: ""
        }
    }

    private fun getMediaSizeExitsMessage(): String {
        val df = DecimalFormat("##.##")
        val size = df.format(getBuilder().selectMediaMaxSize / 1024.0)
        return "${context?.getString(R.string.you_cant_capture_more_than)} $size KB ${context?.getString(
            R.string.size_of
        )} ${getDisplayMediaType()}"
    }

    private fun getSelectCountExitsMessage(): String {
        return "${context?.getString(R.string.you_cant_capture_more_than)} ${getBuilder().selectMediaCount} ${getDisplayMediaType()}"
    }

    private fun openPreviewScreen() {
        openPreviewScreen(mAdapter.getSelectedMedia())
    }

    override fun onBackPressed(): Boolean {
        mAdapter.getCaptureMedia().forEach {
            deleteFile(it.contentUrl)
        }
        activity?.onBackPressed()
        return true
    }

    private fun deleteFile(filePath: String) {
        if (filePath.startsWith("content://")) {
            val contentResolver = activity!!.contentResolver
            contentResolver.delete(Uri.parse(filePath), null, null)
        } else {
            val file = File(filePath)
            if (file.exists()) {
                if (file.delete()) {
                    "File deleted.".loge()
                } else {
                    "Failed to delete file!".loge()
                }
            } else {
                "File not exist!".loge()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_OPEN_PREVIEW_SCREEN -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (getBuilder().selectMedia == SelectMedia.SINGLE) {
                        mAdapter.clearMedia()
                    }
                }
            }
            else -> {

            }
        }
    }

    fun openPreviewScreen(mediaList: ArrayList<Img>) {
        val intent = Intent(context, PreviewAct::class.java)
        intent.putExtra(Constants.EXTRA_MEDIA_LIST, mediaList)
        startActivityForResult(intent, REQUEST_OPEN_PREVIEW_SCREEN)
    }
}