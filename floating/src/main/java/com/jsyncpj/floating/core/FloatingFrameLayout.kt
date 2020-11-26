package com.jsyncpj.floating.core

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/24 13:58
 */
open class FloatingFrameLayout:FrameLayout {
    constructor(context: Context):super(context)
    constructor(context: Context,attrs:AttributeSet?):super(context,attrs)
    constructor(context: Context,attrs: AttributeSet?,defStyleAttr:Int):super(context, attrs, defStyleAttr)
}