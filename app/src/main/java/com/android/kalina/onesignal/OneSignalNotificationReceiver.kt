package com.android.kalina.onesignal

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import com.onesignal.OSNotification
import com.onesignal.OneSignal
import com.android.kalina.util.Logger

/**
 * Created by Dmitriy on 30.01.2018.
 */
class OneSignalNotificationReceiver(val context: Context) : OneSignal.NotificationReceivedHandler {

    private val TAG = OneSignalNotificationReceiver::class.java.name

    override fun notificationReceived(notification: OSNotification) {
        val payload = notification.payload

        val type = OneSignalNotificationHelper.getType(payload)

        Logger.log(TAG, "notificationReceived $type $payload")

        if (!TextUtils.equals(type, "message")) {
            return
        }

        context.sendOrderedBroadcast(createIntent(notification), null)
    }

    private fun createIntent(notification: OSNotification): Intent {
        val payload = notification.payload

        return Intent().apply {
            action = MESSAGE_ACTION
            putExtra(MessageReceiver.TITLE_EXTRA, payload.title)
            putExtra(MessageReceiver.BODY_EXTRA, payload.body)
        }
    }
}