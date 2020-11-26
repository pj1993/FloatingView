package com.jsyncpj.floating.floatingview

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.jsyncpj.floating.R
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.core.AbsFloatingView
import com.jsyncpj.floating.model.FloatingViewLayoutParams

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/25 13:50
 */
class AdsorptionFloatingView : AbsFloatingView() {
    override fun onCreate(context: Context?) {}

    override fun onCreateView(context: Context?, rootView: FrameLayout?): View {
        return LayoutInflater.from(context).inflate(R.layout.floating_adsorption, rootView, false)
    }

    override fun onViewCreated(rootView: FrameLayout?) {
        this.rootView.id = R.id.floating_icon_id
        this.rootView.setOnClickListener {
            ToastUtils.showLong("awsl")
        }
    }

    override fun initFloatingViewLayoutParams(params: FloatingViewLayoutParams?) {
        params?.let {
            it.width = 127
            it.height = 127
            //初始位置是否要保存在本地文件当中,下次打开时保证还是在同一个位置
            //特别是系统的窗起始位置最好在左上角
            it.x = 0
            it.y = 0
        }
    }

    override fun onUp(x: Int, y: Int) {
        super.onUp(x, y)
        //手指离开，吸边
        val screenWidth = if (ScreenUtils.isPortrait()) {
            ScreenUtils.getAppScreenWidth()
        } else {
            ScreenUtils.getAppScreenHeight()
        }

        if (x < screenWidth / 2) {
            startAnimation(x, 0)
        } else {
            startAnimation(x, screenWidth)
        }

    }

    private fun startAnimation(startX: Int, endX: Int) {
        val animator = ValueAnimator.ofInt(startX, endX)
        animator.duration = 500
        animator.addUpdateListener {
            onMove(0, 0, (it.animatedValue as Int) - startX, 0)
            if (it.animatedValue as Int == endX) {
                saveLocationToSp()
            }
        }
        animator.start()
    }

}