package com.android.kalina.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.view.View
import com.android.kalina.R
import com.android.kalina.ui.Studio21Activity
import kotlinx.android.synthetic.main.a_user_agreement.*

class UserAgreementActivity : Studio21Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.a_user_agreement)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.user_agreement)

        val chevronImage = VectorDrawableCompat.create(resources, R.drawable.ic_chevron_left_white_24dp, theme)
        chevronImage?.setTint(ContextCompat.getColor(this, R.color.colorAccent))
        supportActionBar?.setHomeAsUpIndicator(chevronImage)

        webView.settings.javaScriptEnabled = false
        webView.settings.useWideViewPort = false
        webView.settings.builtInZoomControls = true
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.loadUrl("file:///android_asset/User agreement.html")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return false
            }
        }

        return super.onOptionsItemSelected(item)
    }
}