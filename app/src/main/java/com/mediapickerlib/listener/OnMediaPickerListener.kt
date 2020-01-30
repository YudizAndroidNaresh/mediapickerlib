package com.mediapickerlib.listener

import com.mediapickerlib.modals.Img

interface OnMediaPickerListener {
    fun onMediaPick(mediaList: ArrayList<Img>)
}