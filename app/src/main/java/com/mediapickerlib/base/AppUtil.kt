package com.mediapickerlib.base

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mediapickerlib.util.Constants
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object AppUtil {

    val versionAcronym = "v : "

    fun rateApp(context: Activity) {
        val intentToAppstore: Intent
        val url = "https://play.google.com/store/apps/details?id=" + context.packageName
        intentToAppstore = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        try {
            context.startActivity(intentToAppstore)
        } catch (e: Exception) {
            url.openWebPage(context)
        }

    }

    fun shareApp(context: Activity) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + context.packageName)
        if (intent.resolveActivity(context.packageManager) != null)
            context.startActivity(intent)
        else
            "No app found to perform this action".logd()
    }

    fun composeEmail(
        context: Activity,
        addresses: Array<String> = arrayOf(""),
        subject: String = "",
        text: String? = null
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        if (intent.resolveActivity(context.packageManager) != null)
            context.startActivity(intent)
        else
            "You don't have mail client installed".logd()
    }

    fun getHashKey(context: Context): String? {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return Base64.encodeToString(md.digest(), Base64.DEFAULT)
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

        return null
    }

}

fun AppCompatActivity.getVersionCode(): String {
    try {
        return AppUtil.versionAcronym + this.packageManager.getPackageInfo(this.packageName, 0).versionCode
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return ""
}

fun AppCompatActivity.getVersionName(): String {
    try {
        return AppUtil.versionAcronym + this.packageManager.getPackageInfo(this.packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return ""
}

fun AppCompatActivity.hideStatusBar() {
    this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
}

fun Activity.hideKeyboard() {
    currentFocus?.let {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        it.clearFocus()
    }
}

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard()
}

fun View.showKeyboard() {
    requestFocus()
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

fun AppCompatActivity.getDeviceWidth(): Int {
    val displayMetrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun AppCompatActivity.getDeviceHeight(): Int {
    val displayMetrics = DisplayMetrics()
    this.windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun TextView.highlightSubStrings(
    subStringList: List<String>,
    isBold: Boolean,
    color: Int? = null,
    onClick: ((text: String) -> Unit)? = null
) {

    val completeString = this.text.toString()
    val spannableString = SpannableString(completeString)

    subStringList.forEach {

        if (completeString.contains(it, true)) {
            val startPosition = completeString.indexOf(it, ignoreCase = true)
            val endPosition = completeString.lastIndexOf(it, ignoreCase = true) + it.length

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    onClick?.invoke(it)
                }

                override fun updateDrawState(ds: TextPaint?) {
                }
            }

            spannableString.setSpan(clickableSpan, startPosition, endPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

            if (isBold)
                spannableString.setSpan(StyleSpan(Typeface.BOLD), startPosition, endPosition, 0)

            if (color != null)
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, color)),
                    startPosition,
                    endPosition,
                    0
                )

            this.text = spannableString
//        this.setMovementMethod(LinkMovementMethod.getInstance());
            this.highlightColor = Color.TRANSPARENT
            this.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}

fun String.dialNumber(context: Activity) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$this")
    if (intent.resolveActivity(context.packageManager) != null)
        context.startActivity(intent)
    else
        "No app found to perform call action".logd()
}

fun String.openWebPage(context: Activity) {
    val webpage = Uri.parse(this)
    val intent = Intent(Intent.ACTION_VIEW, webpage)
    if (intent.resolveActivity(context.packageManager) != null)
        context.startActivity(intent)
    else
        "No app found to perform this action".logd()
}

fun String.isPackageExists(context: Context): Boolean {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(0)
    for (packageInfo in packages)
        if (packageInfo.packageName == this) return true
    return false
}

fun AppCompatActivity.openCamera() {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    startActivityForResult(intent, Constants.CAPTURE_IMAGE)
}

fun AppCompatActivity.openGallery2() {
    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.SELECT_IMAGE)
}

fun AppCompatActivity.openGallery() {
    var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(intent, Constants.SELECT_IMAGE)
}

fun AppCompatEditText.getData(): String {
    return this.text.toString()
}

fun AppCompatEditText.getTrimData(): String {
    return this.text?.trim().toString()
}

fun Int.toNull(): Int? {
    return if (this == 0) null else this
}

fun addMediaToGallery(context: Context?, mediaPath: String, dateTaken: Long, isVideo: Boolean) {
    val values = ContentValues()
    if (isVideo) {
        values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/*")
        values.put(MediaStore.MediaColumns.DATA, mediaPath)
        context?.contentResolver?.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    } else {
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        values.put(MediaStore.MediaColumns.DATA, mediaPath)
        context?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }
}