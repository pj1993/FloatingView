package com.jsyncpj.floating

import android.app.Application
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.Utils
import com.jsyncpj.floating.core.AbsFloatingView
import com.jsyncpj.floating.core.FloatingManager
import com.jsyncpj.floating.model.FloatingIntent

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 10:30
 */
object FloatingCtrlReal {
    private const val TAG = "FloatingCtrlReal"
    private var APPLICATION: Application? = null


    fun install(app: Application) {
        APPLICATION = app
        initAndroidUtil(app)
        //判断进程
        if (!ProcessUtils.isMainProcess()) {
            return
        }
        //注册生命周期
        app.registerActivityLifecycleCallbacks(FloatingActivityLifecycleCallbacks())
        //初始化管理类
        FloatingManager.instance.init(app)
    }

    private fun initAndroidUtil(app: Application) {
        Utils.init(app)
        LogUtils.getConfig()
            .setLogSwitch(true) // 设置是否输出到控制台开关，默认开
            .setConsoleSwitch(true) // 设置 log 全局标签，默认为空，当全局标签不为空时，我们输出的 log 全部为该 tag， 为空时，如果传入的 tag 为空那就显示类名，否则显示 tag
            .setGlobalTag("Floating") // 设置 log 头信息开关，默认为开
            .setLogHeadSwitch(true) // 打印 log 时是否存到文件的开关，默认关
            .setLog2FileSwitch(true) // 当自定义路径为空时，写入应用的/cache/log/目录中
            .setDir("") // 当文件前缀为空时，默认为"util"，即写入文件为"util-MM-dd.txt"
            .setFilePrefix("djx-table-log") // 输出日志是否带边框开关，默认开
            .setBorderSwitch(true) // 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
            .setSingleTagSwitch(true) // log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
            .setConsoleFilter(LogUtils.V) // log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
            .setFileFilter(LogUtils.E) // log 栈深度，默认为 1
            .setStackDeep(2).stackOffset = 0
    }

    fun showOne(targetClass: Class<out AbsFloatingView?>) {
        val fIntent = FloatingIntent(targetClass)
        fIntent.mode = FloatingIntent.MODE_ONCE
        show(fIntent)
    }

    fun showAlways(targetClass: Class<out AbsFloatingView?>){
        val fIntent = FloatingIntent(targetClass)
        fIntent.mode = FloatingIntent.MODE_SINGLE_INSTANCE
        show(fIntent)
    }

    fun show(floatingIntent: FloatingIntent?) {
        if (floatingIntent?.targetClass?.simpleName.isNullOrBlank() || FloatingManager.instance.getFloatingView(
                ActivityUtils.getTopActivity(),
                floatingIntent?.targetClass?.simpleName
            ) == null
        ) {
            FloatingManager.instance.attach(floatingIntent)
        }
    }

    fun isFloatingShow(targetClass: Class<out AbsFloatingView?>):Boolean{
        return FloatingManager.instance.getFloatingView(ActivityUtils.getTopActivity(),targetClass.simpleName) !=null
    }

    fun hide(targetClass: Class<out AbsFloatingView?>){
        FloatingManager.instance.detach(targetClass)
    }
}