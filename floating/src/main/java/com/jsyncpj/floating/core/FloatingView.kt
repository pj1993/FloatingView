package com.jsyncpj.floating.core

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.jsyncpj.floating.model.FloatingViewLayoutParams

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 16:30
 */
internal interface FloatingView {

    fun onCreate(context: Context?)

    fun onCreateView(context: Context?, rootView: FrameLayout?): View

    fun onViewCreated(rootView: FrameLayout?)

    fun onResume()

    fun onPause()

    /**
     * 确定悬浮窗附表的初始位置
     * layoutParams创建完以后调用
     */
    fun initFloatingViewLayoutParams(params: FloatingViewLayoutParams?)

    /**
     * 进入后台
     * 在这里，你可以处理自己的一些逻辑（可以隐藏系统悬浮窗等等）
     */
    fun onEnterBackground()

    fun onEnterForeground()

    /**
     * 是否可以拖动
     */
    fun canDrag(): Boolean

    /**
     * 是否需要处理返回键
     */
    fun shouldDealBackKey(): Boolean

    /**
     * shouldDealBackKey == true 时调用
     */
    fun onBackPressed(): Boolean

    /**
     * 悬浮窗主动销毁时调用，不能再当前生命周期回调函数中调用，detach自己 否则会出现死循环
     */
    fun onDestroy()
}