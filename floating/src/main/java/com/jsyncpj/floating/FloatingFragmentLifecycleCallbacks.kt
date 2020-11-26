package com.jsyncpj.floating

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.jsyncpj.floating.util.FloatingUtil

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/25 14:10
 */
class FloatingFragmentLifecycleCallbacks : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        super.onFragmentAttached(fm, f, context)
        for (listener in FloatingUtil.lifecycleListeners) {
            listener.onFragmentAttached(f)
        }
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        super.onFragmentDetached(fm, f)
        for (listener in FloatingUtil.lifecycleListeners) {
            listener.onFragmentDetached(f)
        }
    }

}