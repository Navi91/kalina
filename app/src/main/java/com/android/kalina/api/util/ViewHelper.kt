package com.android.kalina.api.util

import android.view.View

fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.visible() {
    this.setVisible(true)
}

fun View.gone() {
    this.setVisible(false)
}