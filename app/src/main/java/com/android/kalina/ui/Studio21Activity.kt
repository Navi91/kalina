package com.android.kalina.ui

import android.support.v7.app.AppCompatActivity
import android.widget.Toast

/**
 * Created by Dmitriy on 21.02.2018.
 */
open class Studio21Activity : AppCompatActivity()  {

    fun showToast(message: String?, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, length).show()
    }
}