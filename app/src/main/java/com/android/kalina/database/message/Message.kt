package com.android.kalina.database.message

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.text.TextUtils
import com.android.kalina.api.util.longHashCode
import java.util.concurrent.TimeUnit

@Entity(primaryKeys = ["id", "client_id"])
class Message() {

    companion object {
        private val UNSENT_MESSAGE_ID = -1L

        fun createUnsentTextMessage(text: String, client_id: String, created_at: Long): Message {
            return Message(UNSENT_MESSAGE_ID, text, "", "", client_id, created_at)
        }

        fun createUnsentAudioMessage(audio: String, client_id: String, created_at: Long): Message {
            return Message(UNSENT_MESSAGE_ID, "", audio, "", client_id, created_at)
        }
    }

    @ColumnInfo(name = "id")
    var id: Long = 0
    @ColumnInfo(name = "prev_id")
    var prev_id: Long = 0
    @ColumnInfo(name = "client_id")
    var client_id: String = ""
    @ColumnInfo(name = "text")
    var text: String = ""
    @ColumnInfo(name = "audio")
    var audio: String? = ""
    @ColumnInfo(name = "created_at")
    var created_at: Long = 0

    fun isIncome() = TextUtils.isEmpty(client_id)

    fun isAudio() = !TextUtils.isEmpty(audio) || TextUtils.isEmpty(text)

    fun isUnsent() = id == UNSENT_MESSAGE_ID

    fun getCreateTimeInMilliseconds() = TimeUnit.SECONDS.toMillis(created_at)

    constructor(id: Long, text: String, audio: String, author: String, client_id: String, created_at: Long) : this() {
        this.id = id;
        this.text = text
        this.audio = audio
        this.client_id = client_id
        this.created_at = created_at
    }

    override fun toString(): String {
        return "Message id: $id text: $text audio: $audio client id: $client_id create at: $created_at"
    }

    override fun hashCode(): Int {
        var hashCode = 17

        hashCode = 31 * hashCode + id.longHashCode()
        hashCode = 31 * hashCode + prev_id.longHashCode()
        hashCode = 31 * hashCode + client_id.hashCode()
        hashCode = 31 * hashCode + text.hashCode()
        hashCode = 31 * hashCode + (audio?.hashCode() ?: 0)
        hashCode = 31 * hashCode + created_at.longHashCode()

        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other !is Message) return false

        if (client_id.isNotEmpty()) {
            return TextUtils.equals(client_id, other.client_id)
        }

        return other.id == id
    }

}