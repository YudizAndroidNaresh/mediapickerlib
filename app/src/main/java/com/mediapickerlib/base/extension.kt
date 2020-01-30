package com.mediapickerlib.base

import android.text.Editable
import android.text.TextWatcher
import android.util.LayoutDirection
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout

fun EditText.manageTogglePassword() = addTextChangedListener(
    object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            (parent.parent as TextInputLayout).isPasswordVisibilityToggleEnabled = !s!!.isEmpty()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
)


fun BaseFragment.addFragment(fragment: BaseFragment, backStackTag: String = "") {
    hideKeyboard()
    if (parentFragment is BaseContainerFragment) {
        (parentFragment as BaseContainerFragment).addFragment(fragment, backStackTag)
    } else if (activity is BaseActivity) {
        (activity as BaseActivity).addFragment(fragment, backStackTag)
    }
}

fun BaseFragment.replaceFragment(fragment: Fragment, backStackTag: String = "") {
    hideKeyboard()
    if (parentFragment is BaseContainerFragment) {
        (parentFragment as BaseContainerFragment).replaceFragment(fragment, backStackTag)
    } else if (activity is BaseActivity) {
        (activity as BaseActivity).replaceFragment(fragment, backStackTag)
    }
}

val BaseFragment.isRTL: Boolean
    get() = resources.configuration.layoutDirection == LayoutDirection.RTL

val BaseActivity.isRTL: Boolean
    get() = resources.configuration.layoutDirection == LayoutDirection.RTL


fun BaseFragment.toast(message: String) {
    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
}

fun BaseActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun BaseContainerFragment.replaceFragment(fragment: Fragment, backStackTag: String = "") {
    hideKeyboard()
    if (backStackTag.isEmpty())
        childFragmentManager.beginTransaction().replace(
            getContainerID(),
            fragment,
            fragment::class.java.simpleName
        ).commit()
    else
        childFragmentManager.beginTransaction().addToBackStack(backStackTag).replace(
            getContainerID(),
            fragment,
            fragment::class.java.simpleName
        ).commit()
}

fun BaseContainerFragment.addFragment(fragment: BaseFragment, backStackTag: String = "") {
    hideKeyboard()
    if (backStackTag.isEmpty())
        childFragmentManager.beginTransaction().add(
            getContainerID(),
            fragment,
            fragment::class.java.simpleName
        ).commit()
    else
        childFragmentManager.beginTransaction().addToBackStack(backStackTag).add(
            getContainerID(),
            fragment,
            fragment::class.java.simpleName
        ).commit()
}


fun BaseActivity.replaceFragment(fragment: Fragment, backStackTag: String = "") {
    hideKeyboard()
    if (getContainerID() == 0) {
        throw Exception("Container not found, call getContainerID() before replaceFragment()")
    } else {
        if (backStackTag.isEmpty())
            supportFragmentManager.beginTransaction().replace(
                getContainerID(),
                fragment,
                fragment::class.java.simpleName
            ).commit()
        else
            supportFragmentManager.beginTransaction().addToBackStack(backStackTag).replace(
                getContainerID(),
                fragment,
                fragment::class.java.simpleName
            ).commit()
    }
}

fun BaseActivity.addFragment(fragment: BaseFragment, backStackTag: String = "") {
    hideKeyboard()
    if (getContainerID() == 0) {
        throw Exception("Container not found, call getContainerID() before addFragment()")
    } else {
        if (backStackTag.isEmpty())
            supportFragmentManager.beginTransaction().add(
                getContainerID(),
                fragment,
                fragment::class.java.simpleName
            ).commit()
        else
            supportFragmentManager.beginTransaction().addToBackStack(backStackTag).add(
                getContainerID(),
                fragment,
                fragment::class.java.simpleName
            ).commit()
    }
}
