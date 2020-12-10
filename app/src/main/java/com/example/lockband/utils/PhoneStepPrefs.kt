package com.example.lockband.utils

import android.content.Context

private const val name = "PHONE_STEPS_KEY"
private const val offsetKey = "PHONE_STEPS_OFFSET"
private const val offsetFixKey = "PHONE_STEPS_OFFSET_FIX"

fun getStepsOffset(context : Context) : Int {
    return getPreferences(context, name).getInt(offsetKey,-1)
}

fun setStepsOffset(context: Context, offset : Int){
    getPreferences(context, name).edit().let {
        it.putInt(offsetKey,offset)
        it.apply()
    }
}

fun getStepsOffsetFix(context : Context) : Int {
    return getPreferences(context, name).getInt(offsetFixKey,-1)
}

fun setStepsOffsetFix(context: Context, offsetFix : Int){
    getPreferences(context, name).edit().let {
        it.putInt(offsetFixKey,offsetFix)
        it.apply()
    }
}
