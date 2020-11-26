package com.jsyncpj.floating.constant

import com.jsyncpj.floating.model.ActivityLifecycleInfo

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 11:00
 */
object FloatingConstant {

    /**
     * 是否是普通的浮标模式
     */
    @JvmField
    var IS_NORMAL_FLOAT_MODE = true

    /**
     * 保存activity的生命周期状态
     */
    @JvmField
    var ACTIVITY_LIFECYCLE_INFOS = mutableMapOf<String, ActivityLifecycleInfo>()

}