package com.jsyncpj.floating.core

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.WindowManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.core.FloatingManager.Companion.instance
import com.jsyncpj.floating.model.ACTIVITY_LIFECYCLE_CREATE2RESUME
import com.jsyncpj.floating.model.FloatingIntent
import com.jsyncpj.floating.util.FloatingUtil

/**
 *@Description:系统悬浮窗的管理者
 *@Author: jsync
 *@CreateDate: 2020/11/23 14:10
 */
internal class SystemFloatingViewManager(val mContext: Context) : FloatingManagerInterface {
    private val TAG = this.javaClass.simpleName

    /**
     * 参考:
     * https://blog.csdn.net/awenyini/article/details/78265284
     * https://yuqirong.me/2017/09/28/Window%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90(%E4%B8%80)%EF%BC%9A%E4%B8%8EDecorView%E7%9A%84%E9%82%A3%E4%BA%9B%E4%BA%8B/
     */
    private val mWindowManager: WindowManager? = instance.windowManager

    /**
     * 所有的floatingView
     */
    private val mFloatingViews: MutableList<AbsFloatingView> by lazy {
        mutableListOf<AbsFloatingView>()
    }

    /**
     * 所有的监听器
     */
    private val mListeners: MutableList<FloatingManager.FloatingViewAttachListener> by lazy {
        mutableListOf<FloatingManager.FloatingViewAttachListener>()
    }


    override fun attach(floatingIntent: FloatingIntent?) {
        try {
            if (floatingIntent?.targetClass == null) {
                return
            }
            if (floatingIntent.mode == FloatingIntent.MODE_SINGLE_INSTANCE) {
                for (mFloatingView in mFloatingViews) {
                    //全局floatingView并且已经添加过了，那么直接返回
                    if (floatingIntent.targetClass.isInstance(mFloatingView)) {
                        return
                    }
                }
            }
            //通过烦死创建floatingView
            val floatingView = floatingIntent.targetClass.newInstance() ?: return
            floatingView.bundle = floatingIntent.bundle
            //添加到floatingViews中去
            mFloatingViews.add(floatingView)
            //创建视图,准备显示前的参数
            floatingView.performCreate(mContext)
            //在window上显示floatingView
            mWindowManager?.addView(floatingView.rootView, floatingView.systemLayoutParams)
            floatingView.onResume()
            //系统悬浮窗的监听回调
            if (!FloatingConstant.IS_NORMAL_FLOAT_MODE) {
                for (mListener in mListeners) {
                    mListener.onFloatingViewAdd(floatingView)
                }
            }
        } catch (e: Exception) {
            LogUtils.d(TAG, e.toString())
        }
    }

    override fun detach(floatingView: AbsFloatingView?) {
        detach(floatingView?.javaClass?.simpleName)
    }

    override fun detach(tag: String?) {
        if (tag.isNullOrBlank() || mWindowManager == null) {
            return
        }
        val it = mFloatingViews.iterator()
        while (it.hasNext()) {
            val floatingView = it.next()
            if (tag == floatingView.tag) {
                floatingView.performDestroy()
                it.remove()
            }
        }
    }

    override fun detach(activity: Activity?, tag: String?) {}
    override fun detach(activity: Activity?, floatingView: AbsFloatingView?) {}
    override fun detach(activity: Activity?, floatingViewClass: Class<out AbsFloatingView?>?) {}
    override fun detach(floatingViewClass: Class<out AbsFloatingView?>?) {
        detach(floatingViewClass?.simpleName)
    }

    override fun detachAll() {
        val it = mFloatingViews.iterator()
        while (it.hasNext()) {
            val floatingView = it.next()
            mWindowManager?.removeView(floatingView.rootView)
            floatingView.performDestroy()
            it.remove()
        }
    }

    override fun getFloatingView(activity: Activity?, tag: String?): AbsFloatingView? {
        if (tag.isNullOrBlank()) {
            return null
        }
        for (mFloatingView in mFloatingViews) {
            if (tag == mFloatingView.tag) {
                return mFloatingView
            }
        }
        return null
    }

    override fun getFloatingViews(activity: Activity?): MutableMap<String, AbsFloatingView?> {
        val floatingViewMaps: MutableMap<String, AbsFloatingView?> = mutableMapOf()
        for (floatingView in mFloatingViews) {
            floatingViewMaps[floatingView.tag] = floatingView
        }
        return floatingViewMaps
    }

    override fun notifyBackground() {
        for (floatingView in mFloatingViews) {
            floatingView.onEnterBackground()
        }
    }

    override fun notifyForeground() {
        for (floatingView in mFloatingViews) {
            floatingView.onEnterForeground()
        }
    }

    /**
     * Activity销毁时调用 不需要实现 为了统一api
     */
    override fun onActivityDestroy(activity: Activity?) {

    }

    override fun resumeAndAttachFloatingViews(activity: Activity?) {
        activity?.let {
            //app启动
            if (FloatingUtil.isOnlyFirstLaunchActivity(it)) {
                //应用启动时，是否加载悬浮窗
                onMainActivityCreate(it)
            }
            val activityLifecycleInfo =
                FloatingConstant.ACTIVITY_LIFECYCLE_INFOS[it.javaClass.canonicalName ?: ""]?:return
            //新建activity
            if (activityLifecycleInfo.activityLifeCycleCount == ACTIVITY_LIFECYCLE_CREATE2RESUME) {
                onActivityCreate(it)
            }
            //activity resume
            if (activityLifecycleInfo.activityLifeCycleCount > ACTIVITY_LIFECYCLE_CREATE2RESUME) {
                onActivityResume(it)
            }
            //生命周期回调
            val floatingViewMap = getFloatingViews(it)
            for (absFloatingView in floatingViewMap.values) {
                absFloatingView?.onResume()
            }
        }
    }


    /**
     * 方法统一
     */
    override fun onMainActivityCreate(activity: Activity?) {}

    override fun onActivityCreate(activity: Activity?) {}

    override fun onActivityResume(activity: Activity?) {}

    override fun onActivityPause(activity: Activity?) {
        val floatingViews: MutableMap<String, AbsFloatingView?> = getFloatingViews(activity)
        for (floatingView in floatingViews.values) {
            floatingView?.onPause()
        }
    }

    fun addFloatingViewAttachedListener(listener: FloatingManager.FloatingViewAttachListener) {
        mListeners.add(listener)
    }

    fun removeFloatingViewAttachedListener(listener: FloatingManager.FloatingViewAttachListener) {
        mListeners.remove(listener)
    }

}