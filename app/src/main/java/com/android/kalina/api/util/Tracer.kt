package com.android.kalina.api.util

import android.util.Log

class Tracer {

    companion object {

        private var enable = true

        fun setEnable(enable: Boolean) {
            this.enable = enable
        }

        fun d(tag: String?, message: String?) {
            trace { Log.d(tag, message) }
        }

        fun e(tag: String?, message: String?) {
            trace { Log.e(tag, message) }
        }

        private fun trace(logger: () -> Unit) {
            if (enable) {
                logger.invoke()
            }
        }
    }
}