package com.smartexpense.ai.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt

class CommonUtils {
    companion object {
        fun stringToColor(
            buttonColor: String?,
            @ColorRes defaultColorResId: Int,
            context: Context?
        ): Int {
            return runCatching {
                buttonColor?.toColorInt()?: 0
            }.getOrElse {
                if (context != null) {
                    ContextCompat.getColor(context, defaultColorResId)
                } else {
                    Color.WHITE
                }
            }
        }
    }
}