package com.jsyncpj.floating

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.core.FloatingManager
import com.jsyncpj.floating.model.ActivityLifecycleInfo
import com.jsyncpj.floating.util.FloatingUtil

/**
 *@Description:全局生命周期回调
 *@Author: jsync
 *@CreateDate: 2020/11/25 14:14
 */
internal class FloatingActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    private var startedActivityCounts = 0
    private var sHasRequestPermission = false

    private val sFragmentLifecycleCallbacks:FragmentManager.FragmentLifecycleCallbacks = FloatingFragmentLifecycleCallbacks()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.let {
            recordActivityLifecycleStatus(it,LIFE_CYCLE_STATUS_CREATE)
            if (it is FragmentActivity){
                //注册fragment生命周期回调
                it.supportFragmentManager.registerFragmentLifecycleCallbacks(sFragmentLifecycleCallbacks,true)
            }
        }
    }

    override fun onActivityStarted(activity: Activity) {
        activity.let {
            if (startedActivityCounts == 0){
                FloatingManager.instance.notifyForeground()
            }
            startedActivityCounts++
        }
    }

    override fun onActivityResumed(activity: Activity) {
        activity.let {
            //记录activity状态
            recordActivityLifecycleStatus(it,LIFE_CYCLE_STATUS_RESUME)
            //添加floatingView
            resumeAndAttachFloatingView(it)
            //监听回调
            for (lifecycleListener in FloatingUtil.lifecycleListeners) {
                lifecycleListener.onActivityResumed(it)
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        activity.let {
            for (lifecycleListener in FloatingUtil.lifecycleListeners) {
                lifecycleListener.onActivityPaused(it)
            }
            FloatingManager.instance.onActivityPause(it)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        activity.let {
            recordActivityLifecycleStatus(it,LIFE_CYCLE_STATUS_STOPPED)
            startedActivityCounts --
            if (startedActivityCounts == 0){//进入后台
                FloatingManager.instance.notifyBackground()
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        activity.let {
            recordActivityLifecycleStatus(it,LIFE_CYCLE_STATUS_DESTROY)
            //注销fragment的生命周期回调
            if (it is FragmentActivity){
                it.supportFragmentManager.unregisterFragmentLifecycleCallbacks(sFragmentLifecycleCallbacks)
            }
            FloatingManager.instance.onActivityDestroy(activity)
        }
    }

    /**
     * 记录当前Activity的生命周期状态
     * onActivityCreated
     * onActivityResumed
     * onActivityStopped
     * onActivityDestroyed
     */
    private fun recordActivityLifecycleStatus(activity: Activity,lifecycleStatus:Int){
        if (activity.javaClass.canonicalName.isNullOrBlank()) return
        var activityLifecycleInfo = FloatingConstant.ACTIVITY_LIFECYCLE_INFOS[activity.javaClass.canonicalName!!]
        if (activityLifecycleInfo == null){
            activityLifecycleInfo = ActivityLifecycleInfo()
            activityLifecycleInfo.activityName = activity.javaClass.canonicalName
            when (lifecycleStatus) {
                LIFE_CYCLE_STATUS_CREATE -> {
                    activityLifecycleInfo.activityLifeCycleCount = 0
                }
                LIFE_CYCLE_STATUS_RESUME -> {
                    activityLifecycleInfo.activityLifeCycleCount +=1
                }
                LIFE_CYCLE_STATUS_STOPPED -> {
                    activityLifecycleInfo.invokeStopMethod = true
                }
            }
            FloatingConstant.ACTIVITY_LIFECYCLE_INFOS[activity.javaClass.canonicalName!!] = activityLifecycleInfo
        }else{
            activityLifecycleInfo.activityName = activity.javaClass.canonicalName
            when (lifecycleStatus) {
                LIFE_CYCLE_STATUS_CREATE -> {
                    activityLifecycleInfo.activityLifeCycleCount = 0
                }
                LIFE_CYCLE_STATUS_RESUME -> {
                    activityLifecycleInfo.activityLifeCycleCount +=1
                }
                LIFE_CYCLE_STATUS_STOPPED -> {
                    activityLifecycleInfo.invokeStopMethod = true
                }
                LIFE_CYCLE_STATUS_DESTROY -> {
                    FloatingConstant.ACTIVITY_LIFECYCLE_INFOS.remove(activity.javaClass.canonicalName!!)
                }
            }
        }
    }

    /**
     * 显示所有应该显示的FloatingView
     */
    private fun resumeAndAttachFloatingView(activity: Activity?){
        activity?.let {
            if (FloatingConstant.IS_NORMAL_FLOAT_MODE){
                FloatingManager.instance.resumeAndAttachFloatingViews(it)
            }else{
                //悬浮窗权限vivo 华为可以不需要动态权限 小米需要
                if (FloatingUtil.canDrawOverlays(it)){
                    FloatingManager.instance.resumeAndAttachFloatingViews(it)
                }else{
                    requestPermission(it)
                }
            }
        }
    }

    private fun requestPermission(context: Context){
        if (!FloatingUtil.canDrawOverlays(context)&&!sHasRequestPermission){
            //请求悬浮权限
            FloatingUtil.requestDrawOverlays(context)
            sHasRequestPermission=true
        }
    }

    companion object {

        /**
         * Activity 创建
         */
        private const val LIFE_CYCLE_STATUS_CREATE = 100

        /**
         * Activity resume
         */
        private const val LIFE_CYCLE_STATUS_RESUME = 101

        /**
         * Activity stop
         */
        private const val LIFE_CYCLE_STATUS_STOPPED = 102

        /**
         * Activity destroy
         */
        private const val LIFE_CYCLE_STATUS_DESTROY = 103
    }

}