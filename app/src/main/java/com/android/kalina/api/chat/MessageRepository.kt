package com.android.kalina.api.chat

import android.arch.lifecycle.LiveData
import com.android.kalina.api.util.Preferences
import com.android.kalina.database.AppDatabase
import com.android.kalina.database.message.Message
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class MessageRepository @Inject constructor(private val database: AppDatabase, private val preferences: Preferences) {

    private val unreadMessageCountBehaviourSubject = BehaviorSubject.create<Int>()

    init {
        notifyUnreadMessageCountChanged()
    }

    fun incrementUnreadMessageCount() {
        preferences.setUnreadMessageCount(preferences.getUnreadMessageCount() + 1)
        notifyUnreadMessageCountChanged()
    }

    fun setUnreadMessageCount(count: Int) {
        preferences.setUnreadMessageCount(count)
        notifyUnreadMessageCountChanged()
    }

    private fun notifyUnreadMessageCountChanged() {
        unreadMessageCountBehaviourSubject.onNext(preferences.getUnreadMessageCount())
    }

    fun getUnreadMessageCountObservable(): Observable<Int> = unreadMessageCountBehaviourSubject

    fun getAllMessageLiveData(): LiveData<List<Message>> {
        return database.messageDao().getAll()
    }

    fun getAllUnsentMessage(): List<Message> {
        return database.messageDao().getAllUnsent()
    }

    fun addMessage(messages: List<Message>) {
        database.messageDao().insertAll(messages)
    }

    fun deleteByClientId(clientId: String) {
        database.messageDao().deleteByClientId(clientId)
    }

    fun updateMessageIdByClientId(id: Long, clientId: String) {
        database.messageDao().updateMessageIdWithClientId(id, clientId)
    }
}