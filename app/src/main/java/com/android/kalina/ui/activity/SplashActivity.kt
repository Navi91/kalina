package com.android.kalina.ui.activity

import android.content.Intent
import android.os.Bundle
import com.android.kalina.ui.Studio21Activity

class SplashActivity : Studio21Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, RadioActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startActivity(intent)
        finish()
    }
}