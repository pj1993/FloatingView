package com.jsyncpj.floating.util

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.os.Process
import com.blankj.utilcode.util.ConvertUtils
import com.jsyncpj.floating.constant.FloatingConstant
import java.lang.reflect.InvocationTargetException

/**
 * dp2px
 */
val Float.dp2px
    get() = ConvertUtils.dp2px(this)

val Int.px2dp
    get() = ConvertUtils.px2dp(this.toFloat())

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 14:45
 */
object FloatingUtil {
    //----------------------------------生命周期相关----------------------------------------------
    var lifecycleListeners: MutableList<LifecycleListener> = ArrayList()

    /**
     * 悬浮窗初始化时注册activity以及fragment生命周期回调监听
     */
    fun registerListener(listener: LifecycleListener) {
        lifecycleListeners.add(listener)
    }

    /**
     * 悬浮窗关闭时注销监听
     */
    fun unRegisterListener(listener: LifecycleListener) {
        lifecycleListeners.remove(listener)
    }

    fun removeAllListener() {
        lifecycleListeners.clear()
    }

    interface LifecycleListener {
        fun onActivityResumed(activity: Activity?)
        fun onActivityPaused(activity: Activity?)
        fun onFragmentAttached(f: Fragment?)
        fun onFragmentDetached(f: Fragment?)
    }

    //---------------------------------------系统信息----------------------------------------------------
    /**
     * 是否是系统main activity
     *
     * @return boolean
     */
    fun isMainLaunchActivity(activity: Activity): Boolean {
        val packageManager = activity.application.packageManager
        val intent = packageManager.getLaunchIntentForPackage(activity.packageName) ?: return false
        val launchComponentName = intent.component
        val componentName = activity.componentName
        return launchComponentName != null && componentName.toString() == launchComponentName.toString()
    }

    /**
     * 是否是系统启动第一次调用mainActivity 页面回退不算
     *
     * @return boolean
     */
    fun isOnlyFirstLaunchActivity(activity: Activity): Boolean {
        val isMainActivity = isMainLaunchActivity(activity)
        val activityLifecycleInfo =
            FloatingConstant.ACTIVITY_LIFECYCLE_INFOS[activity.javaClass.canonicalName]
        return activityLifecycleInfo != null && isMainActivity && !activityLifecycleInfo.invokeStopMethod
    }

    //----------------------------------------权限相关------------------------------------------------------
    /**
     * 判断是否具有悬浮窗权限
     * @param context
     * @return
     */
    fun canDrawOverlays(context: Context): Boolean {
        //android 6.0及以上的判断条件
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else checkOp(context, 24)
        //android 4.4~6.0的判断条件
    }

    private fun checkOp(context: Context, op: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val clazz: Class<*> = AppOpsManager::class.java
            try {
                val method = clazz.getDeclaredMethod(
                    "checkOp",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                return AppOpsManager.MODE_ALLOWED == method.invoke(
                    manager,
                    op,
                    Process.myUid(),
                    context.packageName
                ) as Int
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
        return true
    }

    /**
     * 请求悬浮窗权限
     * @param context
     */
    fun requestDrawOverlays(context: Context) {
        val intent = Intent(
            "android.settings.action.MANAGE_OVERLAY_PERMISSION",
            Uri.parse("package:" + context.packageName)
        )
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            //LogHelper.e(TAG, "No activity to handle intent")
        }
    }

    //----------------------------------------------------------------------------------------------------
}