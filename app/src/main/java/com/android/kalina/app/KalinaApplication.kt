package com.android.kalina.app

import android.app.Application
import android.content.IntentFilter
import android.support.multidex.MultiDexApplication
import android.support.v7.app.AppCompatDelegate
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.onesignal.OneSignal
import com.android.kalina.BuildConfig
import com.android.kalina.api.util.Preferences
import com.android.kalina.api.util.Tracer
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.onesignal.MESSAGE_ACTION
import com.android.kalina.onesignal.MessageReceiver
import com.android.kalina.onesignal.OneSignalNotificationReceiver
import com.android.kalina.ui.activity.RadioActivity
import javax.inject.Inject

/**
 * Created by Dmitriy on 21.02.2018.
 */
class KalinaApplication : MultiDexApplication(), OSSubscriptionObserver {

    @Inject
    lateinit var preferences: Preferences

    override fun onCreate() {
        super.onCreate()

        ComponentHolder.initApplication(this)
        ComponentHolder.applicationComponent().inject(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Tracer.setEnable(BuildConfig.DEBUG)

//        OneSignal.startInit(this).inFocusDisplaying(OneSignal.OSInFocusDisplayOption.None)
////                .setNotificationOpenedHandler { result ->
////                    startActivity(RadioActivity.createOpenChatIntent(this))
////                }
//                .setNotificationReceivedHandler(OneSignalNotificationReceiver(this))
//                .init()

//        OneSignal.addSubscriptionObserver(this)
//        OneSignal.idsAvailable { userId, registrationId ->
//            if (preferences.getPlayerId().isEmpty()) {
//                preferences.setPlayerId(userId)
//            }
//        }
//
//        registerReceiver(MessageReceiver(), IntentFilter(MESSAGE_ACTION))
    }

    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {
        if (stateChanges != null && !stateChanges.from.subscribed && stateChanges.to.subscribed) {
            val playerId = stateChanges.to.userId

            if (playerId != preferences.getPlayerId() && playerId.isNotEmpty()) {
                preferences.setPlayerId(playerId)
            }
        }
    }
}