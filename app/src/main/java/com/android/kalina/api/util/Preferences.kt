package com.android.kalina.api.util

import android.annotation.SuppressLint
import android.content.Context
import javax.inject.Inject

/**
 * Created by Dmitriy on 30.01.2018.
 */
class Preferences @Inject constructor(private val context: Context) {

    private val preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    @SuppressLint("CommitPrefEdits")
    private fun getEditor() = preferences.edit()

    private val VOLUME_PREF = "volume_pref"

    fun setVolume(volume: Float) =
            getEditor().putFloat(VOLUME_PREF, volume).apply()

    fun getVolume(): Float = preferences.getFloat(VOLUME_PREF, 0.4F)

    private val NAME_PREF = "name_pref"

    fun setName(name: String) = getEditor().putString(NAME_PREF, name).apply()

    fun getName(): String = preferences.getString(NAME_PREF, "")

    private val PHONE_PREF = "phone_pref"

    fun setPhone(phone: String) = getEditor().putString(PHONE_PREF, phone).apply()

    fun getPhone(): String = preferences.getString(PHONE_PREF, "")

    private val LAST_MESSAGE_ID_PREF = "last_message_id_pref"

    fun setLastMessageId(id: Long) = getEditor().putLong(LAST_MESSAGE_ID_PREF, id).apply()

    fun getLastMessageId(): Long = preferences.getLong(LAST_MESSAGE_ID_PREF, -1L)

    private val UNREAD_MESSAGE_COUNT_PREF = "unread_message_count_pref"

    fun setUnreadMessageCount(count: Int) = getEditor().putInt(UNREAD_MESSAGE_COUNT_PREF, count).apply()

    fun getUnreadMessageCount(): Int = preferences.getInt(UNREAD_MESSAGE_COUNT_PREF, 0)

    private val DEVICE_CODE_PREF = "auth_token_pref"

    fun setDeviceCode(deviceCode: String) = getEditor().putString(DEVICE_CODE_PREF, deviceCode).apply()

    fun getDeviceCode(): String = preferences.getString(DEVICE_CODE_PREF, "")

    private val MESSAGE_CLIENT_ID_PREF = "message_client_id_pref"

    fun setMessageClientId(id: String) = getEditor().putString(MESSAGE_CLIENT_ID_PREF, id).apply()

    fun getMessageClientId() = preferences.getString(MESSAGE_CLIENT_ID_PREF, "")

    private val MESSAGE_CLIENT_ID_INDEX_PREF = "message_client_id_index_pref"

    fun setMessageClientIdIndex(index: Int) = getEditor().putInt(MESSAGE_CLIENT_ID_INDEX_PREF, index).apply()

    fun getMessageClientIdIndex() = preferences.getInt(MESSAGE_CLIENT_ID_INDEX_PREF, 0)

    private val PLAYER_ID_PREF = "player_id_pref"

    fun setPlayerId(id: String) = getEditor().putString(PLAYER_ID_PREF, id).apply()

    fun getPlayerId(): String = preferences.getString(PLAYER_ID_PREF, "")
}