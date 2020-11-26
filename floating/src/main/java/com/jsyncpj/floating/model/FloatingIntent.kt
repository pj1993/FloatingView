package com.jsyncpj.floating.model

import android.app.Activity
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.jsyncpj.floating.core.AbsFloatingView

/**
 *@Description:悬浮窗标志属性
 *@Author: jsync
 *@CreateDate: 2020/11/23 10:54
 */
class FloatingIntent(val targetClass: Class<out AbsFloatingView?>) {

    var bundle: Bundle? = null

    val tag: String = targetClass.simpleName

    var activity: Activity = ActivityUtils.getTopActivity()

    var mode = MODE_SINGLE_INSTANCE

    /**
     * 拦截掉的activity
     * 哪些activity不显示
     */
    val interceptActivity : MutableList<Class<out Activity>>? = null

    companion object {

        /**
         * 全局的悬浮窗
         */
        const val MODE_SINGLE_INSTANCE = 1

        /**
         * 只在页面创建时显示，页面resume时不恢复
         */
        const val MODE_ONCE = 2
    }

}