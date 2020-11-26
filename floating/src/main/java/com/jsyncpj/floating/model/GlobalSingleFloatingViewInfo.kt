package com.jsyncpj.floating.model

import android.app.Activity
import android.os.Bundle
import com.jsyncpj.floating.core.AbsFloatingView

/**
 *@Description:全局FloatingView的基本信息 由于普通的浮标是每个页面自己管理的
 * 需要有一个map用来保存当前每个类型的FloatingView 便于新开页面和页面resume时的FloatingView添加
 *@Author: jsync
 *@CreateDate: 2020/11/23 18:26
 */
internal data class GlobalSingleFloatingViewInfo(
    val absFloatingViewClass: Class<out AbsFloatingView>,
    val tag: String,
    val mode: Int,
    val bundle: Bundle?,
    /**
     * 拦截掉的activity
     * 哪些activity不显示
     */
    val interceptActivity : MutableList<Class<out Activity>>? = null
)