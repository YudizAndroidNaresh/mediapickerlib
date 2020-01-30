package com.mediapickerlib.util

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


object BindingMethods {

    @BindingAdapter(value = *["loadCircleImage", "placeHolder"], requireAll = false)
    @JvmStatic
    fun loadCircleImage(view: ImageView, imageUrl: String?, placeholder: Drawable?) {
        val requestOptions = RequestOptions()
        requestOptions.circleCrop()
        if (placeholder != null) {
            requestOptions.placeholder(placeholder)
            requestOptions.error(placeholder)

        }
        Glide.with(view.context)
            .load(if (TextUtils.isEmpty(imageUrl)) "" else /*Api.AWS_IMG_BASE_URL +*/ imageUrl)
            .apply(requestOptions)
            .into(view)
    }

    @JvmStatic
    fun loadCircleResourceImage(view: ImageView, respurce: Int?) {
        val requestOptions = RequestOptions()
        requestOptions.circleCrop()
        Glide.with(view.context)
            .load(respurce)
            .apply(requestOptions)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter(value = *["loadDrawableImage"], requireAll = false)
    fun loadDrawableImage(view: ImageView, drawable: Drawable?) {
        val requestOptions = RequestOptions()
        requestOptions.circleCrop()
        Glide.with(view.context)
            .load(drawable)
            .apply(requestOptions)
            .into(view)
    }

    @BindingAdapter(value = *["loadLocalCircleImage", "placeHolder"], requireAll = false)
    @JvmStatic
    fun loadLocalCircleImage(view: ImageView, imageUrl: String?, placeholder: Drawable?) {
        val requestOptions = RequestOptions()
        if (placeholder != null) {
            requestOptions.placeholder(placeholder)
            requestOptions.error(placeholder)
        }
        requestOptions.circleCrop()
        Glide.with(view.context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(view)
    }

    @BindingAdapter(value = *["loadLocalImage", "placeHolder"], requireAll = false)
    @JvmStatic
    fun loadLocalImage(view: ImageView, imageUrl: String?, placeholder: Drawable?) {
        val requestOptions = RequestOptions()
        if (placeholder != null) {
            requestOptions.placeholder(placeholder)
            requestOptions.error(placeholder)
        }
        Glide.with(view.context)
            .load(imageUrl)
            .apply(requestOptions)
            .into(view)
    }
}