package com.android.kalina.api.chat

import com.android.kalina.api.util.Preferences
import java.util.*
import javax.inject.Inject

class MessageClientIdGenerator @Inject constructor(private val preferences: Preferences) {

    private val clientId: String

    init {
        if (preferences.getMessageClientId().isEmpty()) {
            preferences.setMessageClientId(UUID.randomUUID().toString())
        }

        clientId = preferences.getMessageClientId()
    }

    @Synchronized
    fun getNextId(): String {
        val index = preferences.getMessageClientIdIndex()
        val id = "${clientId}_$index"
        preferences.setMessageClientIdIndex(index + 1)

        return id
    }
}