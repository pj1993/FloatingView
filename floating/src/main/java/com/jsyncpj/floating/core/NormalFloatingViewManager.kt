package com.jsyncpj.floating.core

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.jsyncpj.floating.R
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.model.ACTIVITY_LIFECYCLE_CREATE2RESUME
import com.jsyncpj.floating.model.FloatingIntent
import com.jsyncpj.floating.model.GlobalSingleFloatingViewInfo
import com.jsyncpj.floating.util.FloatingUtil
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 *@Description:普通悬浮窗的管理者
 *@Author: jsync
 *@CreateDate: 2020/11/23 14:09
 */
class NormalFloatingViewManager(val mContext: Context) : FloatingManagerInterface {
    private val TAG = this.javaClass.simpleName

    /**
     * 每个activity中floatingView的集合
     */
    private val mActivityFloatingViews: MutableMap<Activity, MutableMap<String, AbsFloatingView?>> by lazy {
        mutableMapOf<Activity, MutableMap<String, AbsFloatingView?>>()
    }

    /**
     * 用于同步保存mActivityFloatingViews中应该在页面上显示的floatingView的集合
     * 即存放所有要显示的全局floatingView（activity#resume的时候使用）
     */
    private val mGlobalSingleFloatingViews: MutableMap<String, GlobalSingleFloatingViewInfo> by lazy {
        mutableMapOf<String, GlobalSingleFloatingViewInfo>()
    }

    override fun attach(floatingIntent: FloatingIntent?) {
        try {
            if (floatingIntent?.activity == null) {
                LogUtils.d(TAG, "activity = null")
                return
            }
            //newInstance方式创建FloatingView
            val floatingView = floatingIntent.targetClass.newInstance()
            //判断当前activity是否存在floatingView
            var floatingViews: MutableMap<String, AbsFloatingView?>? = mActivityFloatingViews[floatingIntent.activity]
            if (floatingViews == null) {
                floatingViews = mutableMapOf()
                mActivityFloatingViews[floatingIntent.activity] = floatingViews
            }
            //判断floatingView是否已经在页面上 同类型的只显示一次
            if (floatingIntent.mode == FloatingIntent.MODE_SINGLE_INSTANCE && floatingViews[floatingIntent.tag] != null) {
                //拿到指定的floatingView并更新位置
                floatingViews[floatingIntent.tag]?.updateViewLayout(floatingIntent.tag, true)
                return
            }
            //当前页面不存在此floatingView，添加进去
            floatingView?.let {
                //配置属性
                it.mode = floatingIntent.mode
                it.bundle = floatingIntent.bundle
                it.tag = floatingIntent.tag
                it.attachActivity = WeakReference(floatingIntent.activity)
                //初始化布局
                it.performCreate(mContext)
                //全局floatingViews中保存该类型
                if (floatingIntent.mode == FloatingIntent.MODE_SINGLE_INSTANCE) {
                    mGlobalSingleFloatingViews[it.tag] = createGlobalSingleFloatingViewInfo(it)
                }
                //获取activity的window的根布局
                val mDecorView = floatingIntent.activity.window.decorView as FrameLayout
                //在根布局中添加floatingView
                if (it.rootView != null) {
                    getFloatingRootContentView(
                        floatingIntent.activity,
                        mDecorView
                    ).addView(it.rootView, it.normalLayoutParams)
                    //延迟100毫秒调用
                    it.postDelayed(100, Runnable {
                        floatingView.onResume()
                    })
                }
            }
            //保存
            floatingViews[floatingIntent.tag] = floatingView
        } catch (e: Exception) {
            LogUtils.d(TAG, e.toString())
        }
    }

    private fun createGlobalSingleFloatingViewInfo(floatingView: AbsFloatingView): GlobalSingleFloatingViewInfo {
        return GlobalSingleFloatingViewInfo(
            floatingView.javaClass,
            floatingView.tag,
            FloatingIntent.MODE_SINGLE_INSTANCE,
            floatingView.bundle
        )
    }

    /**
     * 获取floatingView的根布局
     * @param decorView activity的decorView
     * @return 返回一个全屏的FrameLayout，用于装载我们的floatingView
     */
    private fun getFloatingRootContentView(
        activity: Activity?,
        decorView: FrameLayout
    ): FrameLayout {
        var floatingRootView = decorView.findViewById<FrameLayout>(R.id.floating_content_view_id)
        if (floatingRootView != null) {
            return floatingRootView
        }
        //根布局上没有添加过，则创建一个id是floating_content_view_id的frameLayout
        floatingRootView = FloatingFrameLayout(mContext)
        //普通模式的返回按键监听
        floatingRootView.setOnKeyListener(View.OnKeyListener { _, keyCode, _ ->
            //监听返回键
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                val floatingViews = getFloatingViews(activity)
                if (floatingViews.isNullOrEmpty()) {
                    return@OnKeyListener false
                }
                for (value in floatingViews.values) {
                    if (value?.shouldDealBackKey() == true) {
                        return@OnKeyListener value.onBackPressed()
                    }
                }
            }
            return@OnKeyListener false
        })
        floatingRootView.clipChildren = false
        //解决无法获取返回按键的问题
        floatingRootView.setFocusable(true)
        floatingRootView.setFocusableInTouchMode(true)
        floatingRootView.requestFocus()
        floatingRootView.setId(R.id.floating_content_view_id)
        val floatingParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        try {
            //解决集成SwipeBackLayout而出现的floating入口不显示
            activity?.let {
                if (BarUtils.isStatusBarVisible(it)) {
                    floatingParams.topMargin = BarUtils.getStatusBarHeight()
                }
                if (BarUtils.isSupportNavBar() && BarUtils.isNavBarVisible(it)) {
                    floatingParams.bottomMargin = BarUtils.getNavBarHeight()
                }
            }
        } catch (e: Exception) {
            LogUtils.d(TAG, e.toString())
        }
        floatingRootView.setLayoutParams(floatingParams)
        //添加到activity的根布局上
        decorView.addView(floatingRootView)
        return floatingRootView
    }

    override fun detach(floatingView: AbsFloatingView?) {
        floatingView?.let {
            detach(it.tag)
        }
    }

    override fun detach(activity: Activity?, floatingView: AbsFloatingView?) {
        floatingView?.let {
            detach(activity, it.tag)
        }
    }

    override fun detach(tag: String?) {
        //移除每个activity中指定的floatingView
        for (key in mActivityFloatingViews.keys) {
            val floatingViews = mActivityFloatingViews[key] ?: continue
            val floatingView = floatingViews[tag] ?: continue
            floatingView.rootView?.let {
                it.visibility = View.GONE
                getFloatingRootContentView(
                    floatingView.activity,
                    key.window.decorView as FrameLayout
                ).removeView(it.rootView)
            }
            //移除指定UI
            key.window.decorView.requestLayout()
            floatingView.performDestroy()
            floatingViews.remove(tag)
        }
        //同步全局
        if (mGlobalSingleFloatingViews.containsKey(tag)) {
            mGlobalSingleFloatingViews.remove(tag)
        }
    }

    override fun detach(activity: Activity?, tag: String?) {
        if (activity == null) return
        val floatingViews = mActivityFloatingViews[activity] ?: return
        //找到对应floatingView
        val floatingView = floatingViews[tag] ?: return
        floatingView.rootView?.let {
            it.visibility = View.GONE
            getFloatingRootContentView(
                floatingView.activity,
                activity.window.decorView as FrameLayout
            ).removeView(it)
        }
        activity.window.decorView.requestLayout()
        floatingView.performDestroy()
        //移除记录
        floatingViews.remove(tag)
        if (mGlobalSingleFloatingViews.containsKey(tag)) {
            mGlobalSingleFloatingViews.remove(tag)
        }
    }

    override fun detach(floatingViewClass: Class<out AbsFloatingView?>?) {
        detach(floatingViewClass?.simpleName)
    }

    override fun detach(activity: Activity?, floatingViewClass: Class<out AbsFloatingView?>?) {
        detach(activity, floatingViewClass?.simpleName)
    }

    override fun detachAll() {
        for (key in mActivityFloatingViews.keys) {
            val floatingViews = mActivityFloatingViews[key]
            //移除指定View
            getFloatingRootContentView(key, key.window.decorView as FrameLayout).removeAllViews()
            floatingViews?.clear()
        }
        mGlobalSingleFloatingViews.clear()
    }

    override fun getFloatingView(activity: Activity?, tag: String?): AbsFloatingView? {
        if (tag.isNullOrBlank() || activity == null) return null
        mActivityFloatingViews[activity]?.let {
            return it[tag]
        }
        return null
    }

    override fun getFloatingViews(activity: Activity?): MutableMap<String, AbsFloatingView?>? {
        if (activity == null || mActivityFloatingViews[activity] == null) return mutableMapOf()
        return mActivityFloatingViews[activity]
    }

    override fun notifyBackground() {
        //双层遍历
        mActivityFloatingViews.values.forEach { map ->
            map.values.forEach {
                it?.onEnterBackground()
            }
        }
    }

    override fun notifyForeground() {
        //双层遍历
        mActivityFloatingViews.values.forEach { map ->
            map.values.forEach {
                it?.onEnterForeground()
            }
        }
    }

    override fun onActivityDestroy(activity: Activity?) {
        getFloatingViews(activity)?.let {
            for (value in it.values) {
                value?.performDestroy()
            }
        }
        mActivityFloatingViews.remove(activity)
    }

    override fun resumeAndAttachFloatingViews(activity: Activity?) {
        activity?.let {
            //app启动
            if (FloatingUtil.isOnlyFirstLaunchActivity(it)) {
                //应用启动时，是否加载悬浮窗
                onMainActivityCreate(it)
                return
            }
            val activityLifecycleInfo =
                FloatingConstant.ACTIVITY_LIFECYCLE_INFOS[it.javaClass.canonicalName ?: ""]
                    ?: return
            //新建activity
            if (activityLifecycleInfo.activityLifeCycleCount == ACTIVITY_LIFECYCLE_CREATE2RESUME) {
                onActivityCreate(it)
                return
            }
            //activity resume
            if (activityLifecycleInfo.activityLifeCycleCount > ACTIVITY_LIFECYCLE_CREATE2RESUME) {
                onActivityResume(it)
            }
        }
    }

    override fun onMainActivityCreate(activity: Activity?) {}

    override fun onActivityCreate(activity: Activity?) {
        //将所有的floatingView添加到新建的Activity中去
        for (value in mGlobalSingleFloatingViews.values) {
            //过滤,当前activity是否拦截此floatingView
            if (isInterceptActivity(value, activity)) continue
            val floatingIntent = FloatingIntent(value.absFloatingViewClass)
            floatingIntent.mode = FloatingIntent.MODE_SINGLE_INSTANCE//全局的模式是固定的
            floatingIntent.bundle = value.bundle
            attach(floatingIntent)
        }
    }

    override fun onActivityResume(activity: Activity?) {
        //移除掉当前页面MODE_ONCE的Floating
        val existFloatingViews: MutableMap<String, AbsFloatingView?>? =
            mActivityFloatingViews[activity]
        existFloatingViews?.let {
            //千万注意不要使用for循环去移除对象 下面注释的这段代码存在bug
//            for (AbsDokitView existDokitView : existDokitViews.values()) {
//                if (existDokitView.getMode() == DokitIntent.MODE_ONCE) {
//                    detach(existDokitView.getClass());
//                }
//            }
            val modeOnceFloatingViews: MutableList<String> = mutableListOf()
            existFloatingViews.values.forEach { view ->
                if (view?.mode == FloatingIntent.MODE_ONCE) {
                    modeOnceFloatingViews.add(view.javaClass.simpleName)
                }
            }
            modeOnceFloatingViews.forEach { tag ->
                detach(tag)
            }
        }
        //显示全局的FloatingView
        for (value in mGlobalSingleFloatingViews.values) {
            //过滤
            if (isInterceptActivity(value, activity)) continue
            //判断activity是否存在floatingView
            var existFloatingView: AbsFloatingView? = null
            if (!existFloatingViews.isNullOrEmpty()) {
                existFloatingView = existFloatingViews[value.tag]
            }
            if (existFloatingView?.rootView != null) {
                //存在，就跟新位置
                existFloatingView.rootView.visibility = View.VISIBLE
                existFloatingView.updateViewLayout(existFloatingView.tag, true)
                existFloatingView.onResume()
            } else {
                //添加
                val floatingIntent = FloatingIntent(value.absFloatingViewClass)
                floatingIntent.mode = value.mode
                floatingIntent.bundle = value.bundle
                attach(floatingIntent)
            }
        }
    }

    /**
     * 当前activity是否拦截此floatingView
     */
    private fun isInterceptActivity(
        info: GlobalSingleFloatingViewInfo,
        activity: Activity?
    ): Boolean {
        info.interceptActivity?.let {
            for (clazz in it) {
                if (clazz.isInstance(activity)) {
                    return true
                }
            }
        }
        return false
    }

    override fun onActivityPause(activity: Activity?) {
        val floatingViews: Map<String, AbsFloatingView?>? = getFloatingViews(activity)
        floatingViews?.values?.forEach { it -> it?.onPause() }
    }
}