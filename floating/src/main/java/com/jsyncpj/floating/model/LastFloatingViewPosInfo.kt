package com.jsyncpj.floating.model

import com.blankj.utilcode.util.ScreenUtils

/**
 *@Description:记录FloatingView最后的位置信息
 *@Author: jsync
 *@CreateDate: 2020/11/23 13:50
 */
class LastFloatingViewPosInfo {
    val isPortrait = ScreenUtils.isPortrait()
    var floatingViewWidth = 0
    var floatingViewHeight = 0

    //使用百分比保存最后的信息，防止横竖屏切换的时候位置发生变化
    var leftMarginPercent = 0f
        private set
    var topMarginPercent = 0f
        private set

    fun setLetMargin(leftMargin: Int) {
        leftMarginPercent = leftMargin.toFloat() / ScreenUtils.getAppScreenWidth().toFloat()
    }

    fun setTopMargin(topMargin: Int) {
        topMarginPercent = topMargin.toFloat() / ScreenUtils.getAppScreenHeight().toFloat()
    }

    override fun toString(): String {
        return "LastFloatingViewPosInfo{isPortrait=${isPortrait},lefMarginPercent=${leftMarginPercent},topMarginPercent=${topMarginPercent}}"
    }
}