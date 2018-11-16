package com.android.kalina.api.util

import java.text.SimpleDateFormat
import java.util.*

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

fun Long.getTimeFormatString(): String {
    return timeFormat.format(Date(this))
}