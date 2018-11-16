package com.android.kalina.onesignal

import com.onesignal.OSNotificationPayload

/**
 * Created by Dmitriy on 04.02.2018.
 */
class OneSignalNotificationHelper {

    companion object {
        fun getType(payload: OSNotificationPayload): String = payload.additionalData.optString("type")
    }
}