package com.example.lockband.utils

import android.content.Context

private const val name = "PHONE_STEPS_KEY"
private const val offsetKey = "PHONE_STEPS_OFFSET"

fun getStepsOffset(context : Context) : Int {
    return getPreferences(context, name).getInt(offsetKey,-1)
}

fun setStepsOffset(context: Context, offset : Int){
    getPreferences(context, name).edit().let {
        it.putInt(offsetKey,offset)
        it.apply()
    }
}
