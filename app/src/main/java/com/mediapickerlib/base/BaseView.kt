package com.mediapickerlib.base

interface BaseView {
    fun showErrorMessage(message: String)
    fun showProgress()
    fun hideProgress()
}