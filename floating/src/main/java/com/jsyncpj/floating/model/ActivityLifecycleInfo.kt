package com.jsyncpj.floating.model

/**
 * activityLifeCycleCount = 1 页面创建调用onResume
 * activityLifeCycleCount > 1 页面创建返回onResume
 */
const val ACTIVITY_LIFECYCLE_CREATE2RESUME = 1

/**
 *@Description: 记录activity的生命周期状态信息
 *@Author: jsync
 *@CreateDate: 2020/11/10 11:06
 */
class ActivityLifecycleInfo {
    /**
     * 生命周期是否已经调用过stop 交叉判断是第一次调用resume还是页面返回调用resume
     */
    var invokeStopMethod = false
    var activityName: String? = null

    /**
     * activityLifeCycleCount = 1 页面创建调用onResume
     * activityLifeCycleCount > 1 页面创建返回onResume
     */
    var activityLifeCycleCount = 0

}