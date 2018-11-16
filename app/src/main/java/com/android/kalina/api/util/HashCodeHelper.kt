package com.android.kalina.api.util

fun Long.longHashCode(): Int {
    return this.and(this.shr(32)).toInt()
}