package com.jsyncpj.floating.core

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import com.jsyncpj.floating.FloatingCtrl
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.model.FloatingIntent
import com.jsyncpj.floating.model.LastFloatingViewPosInfo

/**
 *@Description:floatingView的管理者
 *@Author: jsync
 *@CreateDate: 2020/11/23 10:47
 */
class FloatingManager : FloatingManagerInterface {
    companion object {
        val instance: FloatingManager = FloatingManager()
    }

    /**
     * 保存每个floatingView在页面中的位子
     * 只保存 marginLeft 和marginTop
     * 使用的时候是基于窗口的frameLayout，所以知道marginLeft和marginTop就可以确定位置了
     */
    private val mFloatingViewPos: MutableMap<String, Point?> = mutableMapOf()

    /**
     * 保存所有floatingView的最后的位置
     */
    private val mLastFloatingViewPosInfoMaps: MutableMap<String, LastFloatingViewPosInfo> =
        mutableMapOf()

    /**
     * 当前floatingView的管理者
     */
    var mFloatingManager: FloatingManagerInterface? = null

    val windowManager: WindowManager
        get() = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private lateinit var mContext: Context

    fun init(context: Context) {
        mContext = context
        mFloatingManager = if (FloatingConstant.IS_NORMAL_FLOAT_MODE) {
            NormalFloatingViewManager(context)
        } else {
            SystemFloatingViewManager(context)
        }
    }

    /**
     * 只有普通的floatingView才会调用
     * 保存floatingView的位置
     */
    fun saveFloatingViewPos(tag: String, marginLeft: Int, marginTop: Int) {
        val point = mFloatingViewPos[tag]
        if (point == null) {
            mFloatingViewPos[tag] = Point(marginLeft, marginTop)
        } else {
            point.set(marginLeft, marginTop)
        }
    }

    /**
     * 只有普通的floatingView才会调用
     * 获取指定FloatingView的位置
     */
    fun getFloatingViewPos(tag: String): Point? {
        return mFloatingViewPos[tag]
    }

    /**
     * 保存floatingView的最后坐标
     */
    fun saveLastFloatingViewPosInfo(key:String,lastFloatingViewPosInfo: LastFloatingViewPosInfo){
        mLastFloatingViewPosInfoMaps[key] = lastFloatingViewPosInfo
    }

    /**
     * 获取floatingView的最后坐标
     */
    fun getLastFloatingViewPosInfo(key: String):LastFloatingViewPosInfo?{
        return mLastFloatingViewPosInfoMaps[key]
    }

    /**
     * 移除floatingView的最后坐标
     */
    fun removeLastFloatingViewPosInfo(key: String){
        mLastFloatingViewPosInfoMaps.remove(key)
    }

    //--------------------------------------------下面是接口标出方法------------------------------------------------------------------
    override fun attach(floatingIntent: FloatingIntent?) {
        if (FloatingCtrl.APPLICATION == null) return
        mFloatingManager?.attach(floatingIntent)
    }

    override fun detach(floatingView: AbsFloatingView?) {
        mFloatingManager?.detach(floatingView)
    }

    override fun detach(activity: Activity?, floatingView: AbsFloatingView?) {
        mFloatingManager?.detach(activity, floatingView)
    }

    override fun detach(tag: String?) {
        mFloatingManager?.detach(tag)
    }

    override fun detach(activity: Activity?, tag: String?) {
        mFloatingManager?.detach(activity, tag)
    }

    override fun detach(floatingViewClass: Class<out AbsFloatingView?>?) {
        mFloatingManager?.detach(floatingViewClass)
    }

    override fun detach(activity: Activity?, floatingViewClass: Class<out AbsFloatingView?>?) {
        mFloatingManager?.detach(activity, floatingViewClass)
    }

    override fun detachAll() {
        mFloatingManager?.detachAll()
    }

    override fun getFloatingView(activity: Activity?, tag: String?): AbsFloatingView? {
        return mFloatingManager?.getFloatingView(activity, tag)
    }

    override fun getFloatingViews(activity: Activity?): MutableMap<String, AbsFloatingView?>? {
        return mFloatingManager?.getFloatingViews(activity)
    }

    override fun notifyBackground() {
        mFloatingManager?.notifyBackground()
    }

    override fun notifyForeground() {
        mFloatingManager?.notifyForeground()
    }

    override fun onActivityDestroy(activity: Activity?) {
        mFloatingManager?.onActivityDestroy(activity)
    }

    override fun resumeAndAttachFloatingViews(activity: Activity?) {
        mFloatingManager?.resumeAndAttachFloatingViews(activity)
    }

    //这三个方法，包括上面的resumeAndAttachFloatingViews都是在生命周期onActivityResumed方法中处理的
    //floatingView只关心resume和pause状态
    override fun onMainActivityCreate(activity: Activity?) {}
    override fun onActivityCreate(activity: Activity?) {}
    override fun onActivityResume(activity: Activity?) {}

    override fun onActivityPause(activity: Activity?) {
        mFloatingManager?.onActivityPause(activity)
    }

    //-----------------------------------------系统悬浮窗添加移除监听-------------------------------------------------------------

    /**
     * 系统悬浮窗要调用
     */
    interface FloatingViewAttachListener {
        fun onFloatingViewAdd(floatingView: AbsFloatingView?)
    }

    fun addFloatingViewAttachedListener(listener: FloatingViewAttachListener?) {
        if (!FloatingConstant.IS_NORMAL_FLOAT_MODE && mFloatingManager is SystemFloatingViewManager) {
            listener?.let {
                (mFloatingManager as SystemFloatingViewManager).addFloatingViewAttachedListener(listener)
            }
        }
    }

    fun removeFloatingViewAttachedListener(listener: FloatingViewAttachListener?) {
        if (!FloatingConstant.IS_NORMAL_FLOAT_MODE && mFloatingManager is SystemFloatingViewManager) {
            listener?.let {
                (mFloatingManager as SystemFloatingViewManager).removeFloatingViewAttachedListener(listener)
            }
        }
    }


}