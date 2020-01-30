package com.mediapickerlib.base

import android.text.TextUtils
import android.util.Log
import com.mediapickerlib.BuildConfig

val VERBOSE = Log.VERBOSE
val DEBUG = Log.DEBUG
val INFO = Log.INFO
val WARN = Log.WARN
val ERROR = Log.ERROR
val WTF = Log.ASSERT

private var TAG = "TAG"

fun log(level: Int, tag: String?, msg: String, throwable: Throwable?) {
    if (BuildConfig.DEBUG) {
        val elements = Throwable().stackTrace
        var callerClassName = "?"
        var callerMethodName = "?"
        var callerLineNumber = "?"
        if (elements.size >= 4) {
            callerClassName = elements[3].className
            callerClassName = callerClassName.substring(callerClassName.lastIndexOf('.') + 1)
            if (callerClassName.indexOf("$") > 0) {
                callerClassName = callerClassName.substring(0, callerClassName.indexOf("$"))
            }
            callerMethodName = elements[3].methodName
            callerMethodName = callerMethodName.substring(callerMethodName.lastIndexOf('_') + 1)
            if (callerMethodName == "<init>") {
                callerMethodName = callerClassName
            }
            callerLineNumber = elements[3].lineNumber.toString()
        }

        val stack =
            "[$callerClassName.$callerMethodName():$callerLineNumber]" + if (TextUtils.isEmpty(
                    msg
                )
            ) "" else " "

        when (level) {
            VERBOSE -> android.util.Log.v(tag, stack + msg, throwable)
            DEBUG -> android.util.Log.d(tag, stack + msg, throwable)
            INFO -> android.util.Log.i(tag, stack + msg, throwable)
            WARN -> android.util.Log.w(tag, stack + msg, throwable)
            ERROR -> android.util.Log.e(tag, stack + msg, throwable)
            WTF -> android.util.Log.wtf(tag, stack + msg, throwable)
            else -> {
            }
        }
    }
}

fun Any?.logd(tag: String = TAG, throwable: Throwable? = null) {
    log(DEBUG, tag, this.toString(), throwable)
}

fun Any?.loge(tag: String = TAG, throwable: Throwable? = null) {
    log(ERROR, tag, this.toString(), throwable)
}

fun Any?.logv(tag: String = TAG, throwable: Throwable? = null) {
    log(VERBOSE, tag, this.toString(), throwable)
}

fun Any?.logw(tag: String = TAG, throwable: Throwable? = null) {
    log(WARN, tag, this.toString(), throwable)
}

fun Any?.logwtf(tag: String = TAG, throwable: Throwable? = null) {
    log(WTF, tag, this.toString(), throwable)
}

fun Any?.loi(tag: String = TAG, throwable: Throwable? = null) {
    log(INFO, tag, this.toString(), throwable)
}