package com.mediapickerlib.base

abstract class BaseContainerFragment : BaseFragment() {

    abstract fun getContainerID(): Int

    fun popFragment(): Boolean {
        var isPop = false
        try {
            if (childFragmentManager.backStackEntryCount > 0) {
                val tag = childFragmentManager.getBackStackEntryAt(childFragmentManager.backStackEntryCount - 1).name
                val fragment = childFragmentManager.findFragmentByTag(tag)
                if (fragment is BaseFragment && fragment.onBackPressed()) {
                    isPop = true
                } else {
                    isPop = true
                    childFragmentManager.popBackStack()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isPop
    }

    override fun onBackPressed(): Boolean {
        return popFragment()
    }
}