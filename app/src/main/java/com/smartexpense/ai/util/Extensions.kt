package com.smartexpense.ai.util

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun String?.parseColor(context: Context? = null, defaultColor: Int = 0):Int = CommonUtils.stringToColor(this, defaultColor, context)


