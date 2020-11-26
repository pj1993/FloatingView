package com.jsyncpj.floating.core

import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.jsyncpj.floating.util.dp2px
import kotlin.math.abs

/**
 *@Description:手势代理
 *@Author: jsync
 *@CreateDate: 2020/11/10 18:54
 */
class TouchProxy(private var mEventListener: OnTouchEventListener?) {
    private var mLastX = 0
    private var mLastY = 0
    private var mStartX = 0
    private var mStartY = 0
    private var mState = TouchState.STATE_STOP

    private enum class TouchState {
        STATE_MOVE, STATE_STOP
    }

    fun removeListener() {
        mEventListener = null
    }

    fun onTouchEvent(v: View, event: MotionEvent): Boolean {
        val distance = MIN_DISTANCE_MOVE.dp2px
        val x = event.rawX.toInt()//相对屏幕的x轴距  getX是相对View的X轴距离
        val y = event.rawY.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //保存按下时的坐标
                mStartX = x
                mStartY = y
                mLastX = x
                mLastY = y
                mEventListener?.onDown(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                if (abs(x - mStartX) < distance && abs(y - mStartY) < distance) {
                    //不存在移动，且状态是停止状态
                    if (mState == TouchState.STATE_STOP)
                        return true
                } else if (mState != TouchState.STATE_MOVE) {
                    //移动了,且状态不是move，跟新状态
                    mState = TouchState.STATE_MOVE
                }
                //状态是移动状态或者距离大于distance，都会触发move
                mEventListener?.onMove(mLastX, mLastY, x - mLastX, y - mLastY)
                mLastY = y
                mLastX = x
                mState = TouchState.STATE_MOVE//持续move状态
            }
            MotionEvent.ACTION_UP -> {
                mEventListener?.onUp(x, y)
                if (mState != TouchState.STATE_MOVE && event.eventTime - event.downTime < MIN_TAP_TIME) {
                    v.performClick()
                }
                mState = TouchState.STATE_STOP
            }
        }
        return true
    }

    interface OnTouchEventListener {
        /**
         * 移动
         * x:终点坐标
         * dx:起点坐标与终点坐标差值
         */
        fun onMove(x: Int, y: Int, dx: Int, dy: Int)
        fun onUp(x: Int, y: Int)
        fun onDown(x: Int, y: Int)
    }

    companion object {
        private const val MIN_DISTANCE_MOVE = 4f
        private const val MIN_TAP_TIME = 1000
    }

}