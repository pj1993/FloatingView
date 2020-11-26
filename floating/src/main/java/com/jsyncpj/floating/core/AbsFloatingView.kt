package com.jsyncpj.floating.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.IdRes
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.model.FloatingViewLayoutParams
import com.jsyncpj.floating.model.LastFloatingViewPosInfo
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 16:25
 */
abstract class AbsFloatingView : FloatingView, TouchProxy.OnTouchEventListener,
    FloatingManager.FloatingViewAttachListener {
    private val TAG = this.javaClass.simpleName

    /**
     * 标识当前的floatingView,用来当做map的key，和FloatingIntent的tag一致
     */
    var tag = ""

    /**
     * 手势代理
     */
    private val mTouchProxy = TouchProxy(this)

    private val mWindowManager = FloatingManager.instance.windowManager

    /**
     * 创建frameLayout#LayoutParams 内置悬浮窗调用
     */
    lateinit var normalLayoutParams: FrameLayout.LayoutParams

    /**
     * 创建frameLayout#LayoutParams 系统悬浮窗
     */
    lateinit var systemLayoutParams: WindowManager.LayoutParams

    private var mHandler: Handler? = Handler(Looper.myLooper()!!)

    private val mInnerReceiver = InnerReceiver()

    var bundle: Bundle? = null

    var attachActivity: WeakReference<Activity>? = null

    val activity: Activity
        get() {
            return if (attachActivity != null) {
                attachActivity?.get()!!
            } else {
                ActivityUtils.getTopActivity()
            }
        }

    /**
     * floatingView的根布局
     */
    lateinit var rootView: FrameLayout

    /**
     * 系统悬浮窗需要调用
     *
     * @return
     */
    val context: Context?
        get() = this.rootView.context

    /**
     * rootView的直接子View 一般是用户的xml布局 被添加到mRootView中
     */
    private lateinit var mChildView: View

    private lateinit var mFloatingViewLayoutParams: FloatingViewLayoutParams

    /**
     * 上一次floatingView的位置信息
     */
    private lateinit var mLastFloatingViewPosInfo: LastFloatingViewPosInfo

    /**
     * 根布局的实际宽
     */
    private var mFloatingViewWidth = 0

    /**
     * 根布局的实际高
     */
    private var mFloatingViewHeight = 0

    //初始化
    init {
        FloatingManager.instance.getLastFloatingViewPosInfo(tag)?.also {
            mLastFloatingViewPosInfo = it
        } ?: also {
            it.mLastFloatingViewPosInfo = LastFloatingViewPosInfo()
            FloatingManager.instance.saveLastFloatingViewPosInfo(tag, it.mLastFloatingViewPosInfo)
        }
    }

    private val mOnGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener =
        ViewTreeObserver.OnGlobalLayoutListener {
            this@AbsFloatingView.also { floatingView ->
                floatingView.rootView.let { rootView ->
                    floatingView.mFloatingViewHeight = rootView.measuredHeight
                    floatingView.mFloatingViewWidth = rootView.measuredWidth
                    floatingView.mLastFloatingViewPosInfo.apply {
                        floatingViewHeight = floatingView.mFloatingViewHeight
                        floatingViewWidth = floatingView.mFloatingViewWidth
                    }
                }
            }
        }

    /**
     * 页面启动模式
     */
    var mode = 0

    @SuppressLint("ClickableViewAccessibility")
    fun performCreate(context: Context) {
        try {
            onCreate(context)
            if (!isNormalMode) {
                FloatingManager.instance.addFloatingViewAttachedListener(this)
            }
            this.rootView = if (isNormalMode) {
                FloatingFrameLayout(context)
            } else {
                //系统悬浮窗的返回按键
                object : FloatingFrameLayout(context) {
                    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                        if (event?.action == KeyEvent.ACTION_UP && shouldDealBackKey()) {
                            //监听返回按键
                            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_HOME) {
                                return onBackPressed()
                            }
                        }
                        return super.dispatchKeyEvent(event)
                    }
                }
            }
            //添加到根布局的layout回调
            addViewTreeObserverListener()
            //调用onCreateView抽象方法
            mChildView = onCreateView(context, this.rootView)
            //将子view添加到rootView中
            this.rootView.addView(mChildView)
            //设置根布局的手势拦截
            this.rootView.setOnTouchListener { v, event ->
                mTouchProxy.onTouchEvent(v, event)
            }
            //调用onViewCreated回调
            onViewCreated(this.rootView)

            mFloatingViewLayoutParams = FloatingViewLayoutParams()
            //分别创建对应的LayoutParams
            if (isNormalMode) {
                normalLayoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                normalLayoutParams.gravity = Gravity.LEFT or Gravity.TOP
                mFloatingViewLayoutParams.gravity = Gravity.LEFT or Gravity.TOP
            } else {
                systemLayoutParams = WindowManager.LayoutParams()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //8.0适配
                    systemLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    systemLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
                }
                if (!shouldDealBackKey()) {
                    //参考：http://www.shirlman.com/tec/20160426/362
                    //设置WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE会导致rootview监听不到返回按键的监听失效
                    systemLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    mFloatingViewLayoutParams.flags = FloatingViewLayoutParams.FLAG_NOT_FOCUSABLE
                }
                systemLayoutParams.format = PixelFormat.TRANSPARENT
                systemLayoutParams.gravity = Gravity.LEFT or Gravity.TOP
                systemLayoutParams.gravity = Gravity.LEFT or Gravity.TOP
                //动态注册关闭系统弹窗的广播
                val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                context.registerReceiver(mInnerReceiver, intentFilter)
            }
            //创建floatingView的layoutParams
            initFloatingViewLayoutParams(mFloatingViewLayoutParams)
            if (isNormalMode) {
                onNormalLayoutParamsCreated(normalLayoutParams)
            } else {
                onSystemLayoutParamsCreated(systemLayoutParams)
            }

        } catch (e: Exception) {
            LogUtils.d(TAG, e.toString())
        }
    }

    fun performDestroy() {
        if (!isNormalMode) {
            context?.unregisterReceiver(mInnerReceiver)
        }
        //移除监听
        removeViewTreeObserverListener()
        mHandler = null
        onDestroy()
    }

    private fun addViewTreeObserverListener() {
        rootView.viewTreeObserver?.addOnGlobalLayoutListener(mOnGlobalLayoutListener)
    }

    private fun removeViewTreeObserverListener() {
        rootView.viewTreeObserver?.let {
            if (it.isAlive) {
                it.removeOnGlobalLayoutListener(mOnGlobalLayoutListener)
            }
        }
    }

    /**
     * 确定普通浮标的初始位置
     */
    private fun onNormalLayoutParamsCreated(params: FrameLayout.LayoutParams?) {
        params?.let {
            it.width = mFloatingViewLayoutParams.width
            it.height = mFloatingViewLayoutParams.height
            it.gravity = mFloatingViewLayoutParams.gravity
            portraitOrLandscape(it)
        }
    }

    private fun onSystemLayoutParamsCreated(params: WindowManager.LayoutParams?) {
        params?.apply {
            flags = mFloatingViewLayoutParams.flags
            gravity = mFloatingViewLayoutParams.gravity
            width = mFloatingViewLayoutParams.width
            height = mFloatingViewLayoutParams.height
            val point = FloatingManager.instance.getFloatingViewPos(tag)
            if (point != null) {
                x = point.x
                y = point.y
            } else {
                x = mFloatingViewLayoutParams.x
                y = mFloatingViewLayoutParams.y
            }
        }
    }

    private fun portraitOrLandscape(params: FrameLayout.LayoutParams?) {
        params?.let {
            val point = FloatingManager.instance.getFloatingViewPos(tag)
            if (point != null) {
                //横竖屏切换
                if (ScreenUtils.isPortrait()) {
                    if (mLastFloatingViewPosInfo.isPortrait) {
                        it.leftMargin = point.x
                        it.topMargin = point.y
                    } else {
                        it.leftMargin =
                            (point.x * mLastFloatingViewPosInfo.leftMarginPercent).toInt()
                        it.topMargin = (point.y * mLastFloatingViewPosInfo.topMarginPercent).toInt()
                    }
                } else {
                    if (mLastFloatingViewPosInfo.isPortrait) {
                        it.leftMargin =
                            (point.x * mLastFloatingViewPosInfo.leftMarginPercent).toInt()
                        it.topMargin = (point.y * mLastFloatingViewPosInfo.topMarginPercent).toInt()
                    } else {
                        it.leftMargin = point.x
                        it.topMargin = point.y
                    }
                }
            } else {
                if (ScreenUtils.isPortrait()) {
                    if (mLastFloatingViewPosInfo.isPortrait) {
                        it.leftMargin = mFloatingViewLayoutParams.x
                        it.topMargin = mFloatingViewLayoutParams.y
                    } else {
                        it.leftMargin =
                            (mFloatingViewLayoutParams.x * mLastFloatingViewPosInfo.leftMarginPercent).toInt()
                        it.topMargin =
                            (mFloatingViewLayoutParams.y * mLastFloatingViewPosInfo.topMarginPercent).toInt()
                    }
                } else {
                    if (mLastFloatingViewPosInfo.isPortrait) {
                        it.leftMargin =
                            (mFloatingViewLayoutParams.x * mLastFloatingViewPosInfo.leftMarginPercent).toInt()
                        it.topMargin =
                            (mFloatingViewLayoutParams.y * mLastFloatingViewPosInfo.topMarginPercent).toInt()
                    } else {
                        it.leftMargin = mFloatingViewLayoutParams.x
                        it.topMargin = mFloatingViewLayoutParams.y
                    }
                }
            }
            mLastFloatingViewPosInfo.setLetMargin(it.leftMargin)
            mLastFloatingViewPosInfo.setTopMargin(it.topMargin)
        }
    }


    override fun onResume() {
    }

    override fun onPause() {
    }


    override fun onEnterBackground() {
    }

    override fun onEnterForeground() {
    }

    /**
     * 默认实现为true
     */
    override fun canDrag(): Boolean {
        return true
    }

    override fun shouldDealBackKey(): Boolean {
        return false
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroy() {
        if (!isNormalMode) {
            FloatingManager.instance.removeFloatingViewAttachedListener(this)
        }
        FloatingManager.instance.removeLastFloatingViewPosInfo(tag)
        attachActivity = null
        mTouchProxy.removeListener()
    }


    //---------------------------------------------------------------------------------------------
    fun detach() {
        FloatingManager.instance.detach(this)
    }

    protected fun <T : View> findViewById(@IdRes id: Int): T {
        return this.rootView.findViewById(id)
    }

    val isShow: Boolean
        get() = this.rootView.isShown

    fun post(runnable: Runnable) {
        runnable?.let {
            mHandler?.post(runnable)
        }
    }

    fun postDelayed(delayTime: Long, runnable: Runnable) {
        runnable?.let {
            mHandler?.postDelayed(runnable, delayTime)
        }
    }

    /**
     * 更新view的位置
     *  仅限普通浮层
     */
    open fun updateViewLayout(tag: String, isActivityResume: Boolean) {
        if (!isNormalMode) return

        if (isActivityResume) {
            val point = FloatingManager.instance.getFloatingViewPos(tag)
            point?.let {
                normalLayoutParams.leftMargin = it.x
                normalLayoutParams.topMargin = it.y
            }
        } else {
            //非页面切换的时候保存当前位置信息
            mLastFloatingViewPosInfo.setLetMargin(normalLayoutParams.leftMargin)
            mLastFloatingViewPosInfo.setTopMargin(normalLayoutParams.topMargin)
        }

        normalLayoutParams.width = mFloatingViewWidth
        normalLayoutParams.height = mFloatingViewHeight

        resetBorderline(normalLayoutParams)
        this.rootView.layoutParams = normalLayoutParams
    }

    private val screenLongSideLength: Int
        get() = if (ScreenUtils.isPortrait()) {
            ScreenUtils.getAppScreenHeight()
        } else {
            ScreenUtils.getAppScreenWidth()
        }

    private val screenShortSideLength: Int
        get() = if (ScreenUtils.isPortrait()) {
            ScreenUtils.getAppScreenWidth()
        } else {
            ScreenUtils.getAppScreenHeight()
        }

    /**
     * 限制边界 调用的时候必须保证是在控件能获取到宽高德前提下
     * @param params normalLayoutParams
     */
    private fun resetBorderline(params: FrameLayout.LayoutParams) {
        if (!restrictBorderline() || !isNormalMode) return
        //限制y
        if (params.topMargin <= 0) {
            params.topMargin = 0
        }
        if (ScreenUtils.isPortrait()) {
            if (params.topMargin >= screenLongSideLength - mFloatingViewHeight) {
                params.topMargin = screenLongSideLength - mFloatingViewHeight
            }
        } else {
            if (params.topMargin >= screenShortSideLength - mFloatingViewHeight) {
                params.topMargin = screenShortSideLength - mFloatingViewHeight
            }
        }
        //限制x
        if (params.leftMargin <= 0) {
            params.leftMargin = 0
        }
        if (ScreenUtils.isPortrait()) {
            if (params.leftMargin >= screenShortSideLength - mFloatingViewWidth) {
                params.leftMargin = screenShortSideLength - mFloatingViewWidth
            }
        } else {
            if (params.leftMargin >= screenLongSideLength - mFloatingViewWidth) {
                params.leftMargin = screenLongSideLength - mFloatingViewWidth
            }
        }
    }

    /**
     * 系统的中心点是0,0
     */
    private fun resetBorderline(params: WindowManager.LayoutParams){
        if (!restrictBorderline() || isNormalMode) return
        //限制y
        if (ScreenUtils.isPortrait()) {
            if (params.y <= -screenLongSideLength/2) {
                params.y = -screenLongSideLength/2
            }
            if (params.y >= (screenLongSideLength - mFloatingViewHeight)/2) {
                params.y = (screenLongSideLength - mFloatingViewHeight)/2
            }
        } else {
            if (params.y <= -screenShortSideLength/2) {
                params.y = -screenShortSideLength/2
            }
            if (params.y >= (screenShortSideLength - mFloatingViewHeight)/2) {
                params.y = (screenShortSideLength - mFloatingViewHeight)/2
            }
        }
        //限制x
        if (ScreenUtils.isPortrait()) {
            if (params.x <= -screenShortSideLength/2) {
                params.x = -screenShortSideLength/2
            }
            if (params.x >= (screenShortSideLength - mFloatingViewWidth)/2) {
                params.x = (screenShortSideLength - mFloatingViewWidth)/2
            }
        } else {
            if (params.x <= -screenLongSideLength/2) {
                params.x = -screenLongSideLength/2
            }
            if (params.x >= (screenLongSideLength - mFloatingViewWidth)/2) {
                params.x = (screenLongSideLength - mFloatingViewWidth)/2
            }
        }
    }

    override fun onDown(x: Int, y: Int) {
        if (!canDrag()) return
    }

    override fun onMove(x: Int, y: Int, dx: Int, dy: Int) {
        if (!canDrag()) return
        if (isNormalMode) {
            normalLayoutParams.leftMargin += dx
            normalLayoutParams.topMargin += dy
            //更新
            updateViewLayout(tag, false)
        } else {
            systemLayoutParams.x += dx
            systemLayoutParams.y += dy
            //限制边界
            resetBorderline(systemLayoutParams)
            mWindowManager.updateViewLayout(this.rootView, systemLayoutParams)
            LogUtils.d(TAG,"dx=${dx},dy=${dy}，systemLayoutParams.x=${systemLayoutParams.x}")
        }
    }

    override fun onUp(x: Int, y: Int) {
        if (!canDrag()) return
        //保存最后点位置
        saveLocationToSp()
    }

    protected fun saveLocationToSp() {
        if (isNormalMode) {
            FloatingManager.instance.saveFloatingViewPos(
                tag,
                normalLayoutParams.leftMargin,
                normalLayoutParams.topMargin
            )
        } else {
            FloatingManager.instance.saveFloatingViewPos(
                tag,
                systemLayoutParams.x,
                systemLayoutParams.y
            )
        }
    }

    /**
     * 广播接收器 系统悬浮窗需要调用
     */
    protected inner class InnerReceiver : BroadcastReceiver() {
        val SYSTEM_DIALOG_REASON_KEY = "reason"
        val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
        val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS == action) {
                val reason = intent?.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
                reason?.let {
                    if (it == SYSTEM_DIALOG_REASON_HOME_KEY) {
                        //点击home键
                        onHomeKeyPress()
                    } else if (it == SYSTEM_DIALOG_REASON_RECENT_APPS) {
                        //点击menu按钮
                        onRecentAppKeyPress()
                    }
                }
            }
        }

    }

    /**
     * home键被点击 只有系统悬浮窗控件才会被调用
     */
    open fun onHomeKeyPress() {}

    /**
     * 菜单键被点击 只有系统悬浮窗控件才会被调用
     */
    open fun onRecentAppKeyPress() {}

    /**
     * 不能在改方法中进行dokitview的添加和删除 因为处于遍历过程在
     * 只有系统模式下才会调用
     */
    override fun onFloatingViewAdd(floatingView: AbsFloatingView?) {}

    /**
     * 是否限制布局边界
     */
    open fun restrictBorderline():Boolean{
        return true
    }

    /**
     * 设置当前不响应触摸事件
     * 控件默认响应触摸事件
     * 需要在子view的onViewCreated中调用
     */
    fun setFloatingViewNotResponseTouchEvent(view:View?){
        if (isNormalMode){
            view?.setOnTouchListener { _, _ ->false}
        }else{
            view?.setOnTouchListener (null)
        }
    }

    /**
     * 是否是普通的浮标模式
     *
     * @return
     */
    val isNormalMode: Boolean
        get() = FloatingConstant.IS_NORMAL_FLOAT_MODE
    /**
     * 强制刷新
     */
    open fun invalidate(){
        this.rootView.layoutParams = normalLayoutParams
    }
}