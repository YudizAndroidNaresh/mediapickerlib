package com.mediapickerlib.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.mediapickerlib.R

class MyPreference(internal var context: Context) {
    private var myPrefs: SharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), 0)
    private var prefEditor: SharedPreferences.Editor

    init {
        @SuppressLint("CommitPrefEdits")
        prefEditor = myPrefs.edit()
    }

    fun clearPrefs() {
        prefEditor.clear()
        prefEditor.commit()
    }

    // -----------------------------------------------------------------

    operator fun set(key: Keys, value: String?) {
        prefEditor.putString(key.name, value)
        prefEditor.commit()
    }


    operator fun set(key: Keys, value: Int) {
        prefEditor.putInt(key.name, value)
        prefEditor.commit()
    }

    operator fun set(key: Keys, value: Boolean) {
        prefEditor.putBoolean(key.name, value)
        prefEditor.commit()
    }

    operator fun get(key: Keys): String {
        return myPrefs.getString(key.name, "") ?: ""
    }


    fun getInt(key: Keys): Int {
        return myPrefs.getInt(key.name, 0)
    }

    operator fun get(key: String): String {
        return myPrefs.getString(key, "") ?: ""
    }

    fun getBool(key: Keys): Boolean {
        return myPrefs.getBoolean(key.name, false)
    }

    enum class Keys {
        TOKEN, PROFILE, IS_LOGIN, IS_RUNNING_PAYMENT
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: MyPreference? = null

        fun init(context: Context) {
            instance = MyPreference(context)
        }
    }
}
