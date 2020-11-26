package com.jsyncpj.floatingview

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.jsyncpj.floating.core.AbsFloatingView
import com.jsyncpj.floating.core.FloatingManager
import com.jsyncpj.floating.model.FloatingViewLayoutParams
import com.jsyncpj.floating.util.dp2px
import com.jsyncpj.floatingview.entity.MessageBean
import java.time.Duration

/**
 *@Description:一次显示，normal
 * 5秒自动销毁，上滑消失,点击跳转
 *@Author: jsync
 *@CreateDate: 2020/11/25 18:25
 */
class MessageFloatingView : AbsFloatingView() {
    override fun onCreate(context: Context?) {

    }

    override fun onCreateView(context: Context?, rootView: FrameLayout?): View {
        return LayoutInflater.from(context).inflate(R.layout.floating_message, rootView, false)
    }

    override fun onViewCreated(rootView: FrameLayout?) {
        bundle?.let { b->
            this.rootView.id = R.id.floating_icon_id
            this.rootView.setOnClickListener {
                val m :MessageBean = b.getSerializable("MessageFloatingView") as MessageBean?:return@setOnClickListener
                ToastUtils.showLong(m.name)
            }
        }
    }

    override fun initFloatingViewLayoutParams(params: FloatingViewLayoutParams?) {
        params?.let {
            it.width = WindowManager.LayoutParams.MATCH_PARENT
            it.height = 80f.dp2px
            //初始位置是顶部
            it.x = 0
            it.y = (-80f).dp2px
        }
    }

    //屏蔽掉父View的滑动
    override fun canDrag(): Boolean {
        return false
    }

    //不限制边界
    override fun restrictBorderline(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        startAnimation((-80f).dp2px, 0,800)
    }


    override fun onMove(x: Int, y: Int, dx: Int, dy: Int) {
        super.onMove(x, y, dx, dy)
        //上滑动,消失
        moveSelf(dy)
    }

    override fun onUp(x: Int, y: Int) {
        //判断是否要退出
        if (normalLayoutParams.topMargin> (-45f).dp2px){
            //弹回
            startAnimation(normalLayoutParams.topMargin,0,500)
        }else{
            //删除
            startAnimation(normalLayoutParams.topMargin,(-80f).dp2px,500)
        }
    }

    private fun moveSelf(dy: Int) {
        normalLayoutParams.topMargin += dy
        if (normalLayoutParams.topMargin < (-80f).dp2px) {
            normalLayoutParams.topMargin = (-80f).dp2px
        }
        if (normalLayoutParams.topMargin > 0) {
            normalLayoutParams.topMargin = 0
        }
        //更新位置
        updateViewLayout(tag, false)
    }

    /**
     * -80dp ~ 0dp
     * 0dp ~ -80dp
     */
    private fun startAnimation(startY: Int, endY: Int,duration: Long) {
        val animator = ValueAnimator.ofInt(startY, endY)
        animator.duration = duration
        animator.addUpdateListener {
            moveSelf((it.animatedValue as Int) - startY)
            if (it.animatedValue as Int == endY) {
                if (endY == 0) {
//                    saveLocationToSp()
                } else {
                    //销毁自己
                    LogUtils.d("myFloating","销毁调用")
                    FloatingManager.instance.detach(MessageFloatingView::class.java)
                }
            }
        }
        animator.start()
    }

    override fun onDestroy() {
        LogUtils.d("myFloating","销毁")
        super.onDestroy()
    }

}