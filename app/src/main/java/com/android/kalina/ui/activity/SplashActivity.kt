package com.android.kalina.ui.activity

import android.content.Intent
import android.os.Bundle
import com.android.kalina.ui.KalinaActivity

class SplashActivity : KalinaActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(Intent(this, RadioActivity::class.java)) {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(this)
            finish()
        }

    }
}