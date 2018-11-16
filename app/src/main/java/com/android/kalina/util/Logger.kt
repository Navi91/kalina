package com.android.kalina.util

import android.util.Log

/**
 * Created by Dmitriy on 25.02.2018.
 */
class Logger {

    companion object {
        fun log(tag: String?, value: String?) {
            Log.d(tag, value)
        }
    }
}