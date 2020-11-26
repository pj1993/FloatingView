package com.jsyncpj.floating.config

import com.blankj.utilcode.util.SPUtils
import com.jsyncpj.floating.constant.FLOAT_ICON_POS_X
import com.jsyncpj.floating.constant.FLOAT_ICON_POS_Y

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 10:52
 */

fun getFloatViewLastPosX(): Int {
    return SPUtils.getInstance().getInt(FLOAT_ICON_POS_X, 0)
}

fun getFloatViewLastPosY(): Int {
    return SPUtils.getInstance().getInt(FLOAT_ICON_POS_Y, 0)
}

fun saveFloatViewLastPosX(x: Int) {
    return SPUtils.getInstance().put(FLOAT_ICON_POS_X, x)
}

fun saveFloatViewLastPosY(y: Int) {
    return SPUtils.getInstance().put(FLOAT_ICON_POS_Y, y)
}
