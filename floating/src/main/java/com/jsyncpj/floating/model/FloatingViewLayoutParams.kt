package com.jsyncpj.floating.model

import android.view.ViewGroup
import android.view.WindowManager

/**
 *@Description: 暂存放floatingView的参数，最终会设置到 systemLayoutParams或者normalLayoutParams中
 *@Author: jsync
 *@CreateDate: 2020/11/10 17:00
 */
data class FloatingViewLayoutParams(
    var flags: Int = 0,
    var gravity: Int = 0,
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0
) {
    companion object{
        /**
         * 悬浮窗不能获取焦点
         */
        const val FLAG_NOT_FOCUSABLE = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        /**
         * 悬浮窗不能获取焦点并且不相应触摸
         */
        const val FLAG_NOT_FOCUSABLE_AND_NOT_TOUCHABLE = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT

        const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}