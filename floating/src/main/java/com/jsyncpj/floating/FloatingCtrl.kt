package com.jsyncpj.floating

import android.app.Application
import android.content.Context
import com.jsyncpj.floating.constant.FloatingConstant
import com.jsyncpj.floating.core.AbsFloatingView
import com.jsyncpj.floating.core.FloatingManager
import com.jsyncpj.floating.model.FloatingIntent
import com.jsyncpj.floating.util.FloatingUtil
import java.lang.Exception

/**
 *@Description:
 *@Author: jsync
 *@CreateDate: 2020/11/10 10:30
 */
public object FloatingCtrl{
    @JvmField
    var APPLICATION : Application?=null
    private const val TAG = "FloatingCtrl"


    @JvmStatic
    fun install(app:Application){
        APPLICATION = app
        try {
            FloatingCtrlReal.install(app)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    @JvmStatic
    fun showOnce(context: Context,targetClass:Class<out AbsFloatingView?>){
        if (FloatingConstant.IS_NORMAL_FLOAT_MODE || FloatingUtil.canDrawOverlays(context)){
            FloatingCtrlReal.showOne(targetClass)
        }else{
            FloatingUtil.requestDrawOverlays(context)
        }
    }
    @JvmStatic
    fun showAlways(context: Context,targetClass:Class<out AbsFloatingView?>){
        if (FloatingConstant.IS_NORMAL_FLOAT_MODE || FloatingUtil.canDrawOverlays(context)){
            FloatingCtrlReal.showAlways(targetClass)
        }else{
            FloatingUtil.requestDrawOverlays(context)
        }
    }
    @JvmStatic
    fun show(context: Context,floatingIntent: FloatingIntent?){
        if (FloatingConstant.IS_NORMAL_FLOAT_MODE || FloatingUtil.canDrawOverlays(context)){
            FloatingCtrlReal.show(floatingIntent)
        }else{
            FloatingUtil.requestDrawOverlays(context)
        }
    }

    @JvmStatic
    fun hide(targetClass: Class<out AbsFloatingView?>){
        FloatingCtrlReal.hide(targetClass)
    }

    @JvmStatic
    fun setSystemFloat(boolean: Boolean){
        FloatingManager.instance.mFloatingManager?.detachAll()
        FloatingConstant.IS_NORMAL_FLOAT_MODE = !boolean
    }

}