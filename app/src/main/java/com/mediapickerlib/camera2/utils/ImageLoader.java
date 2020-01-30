package com.mediapickerlib.camera2.utils;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class ImageLoader {

    public static void loadCircleImage(ImageView view, Uri imageUrl) {
        Glide.with(view.getContext())
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(view);
    }

    public static void loadImage(ImageView view, Uri imageUrl) {
        Glide.with(view.getContext()).load(imageUrl).apply(new RequestOptions()).into(view);
    }

    public static void loadImage(ImageView view, String imageUrl) {
        Glide.with(view.getContext()).load(imageUrl).apply(new RequestOptions()).into(view);
    }
}
