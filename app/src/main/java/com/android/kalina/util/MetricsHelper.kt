package com.android.kalina.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

fun Context.toPx(dp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics).toInt()
}

class MetricsHelper {

    companion object {
        fun toPx(resources: Resources, dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
    }
}