package com.jsyncpj.floating.core

import android.app.Activity
import com.jsyncpj.floating.model.FloatingIntent

/**
 *@Description: floatingView的管理类标准接口
 *@Author: jsync
 *@CreateDate: 2020/11/23 10:48
 */
interface FloatingManagerInterface {
    /**
     * 再当前activity中添加指定悬浮窗
     */
    fun attach(floatingIntent: FloatingIntent?)

    /**
     * 移除每个activity指定的floatingView
     */
    fun detach(floatingView: AbsFloatingView?)

    fun detach(activity: Activity?, floatingView: AbsFloatingView?)

    fun detach(tag: String?)

    fun detach(activity: Activity?, tag: String?)

    fun detach(floatingViewClass: Class<out AbsFloatingView?>?)

    fun detach(activity: Activity?, floatingViewClass: Class<out AbsFloatingView?>?)

    /**
     * 移除所有activity的所有floatingView
     */
    fun detachAll()

    /**
     * 获取页面上指定的floatingView
     */
    fun getFloatingView(activity: Activity?, tag: String?): AbsFloatingView?

    /**
     * 获取页面上所有的floatingView
     */
    fun getFloatingViews(activity: Activity?): MutableMap<String, AbsFloatingView?>?

    /**
     * app进入后台
     */
    fun notifyBackground()

    /**
     * app进入前台
     */
    fun notifyForeground()

    /**
     * activity销毁
     */
    fun onActivityDestroy(activity: Activity?)

    /**
     * 针对普通的floatingView
     * 添加activity关联的所有FloatingView
     */
    fun resumeAndAttachFloatingViews(activity: Activity?)

    /**
     * mainActivity创建
     */
    fun onMainActivityCreate(activity: Activity?)

    /**
     * 除了mainActivity其他activity创建
     */
    fun onActivityCreate(activity: Activity?)

    fun onActivityResume(activity: Activity?)

    fun onActivityPause(activity: Activity?)

}