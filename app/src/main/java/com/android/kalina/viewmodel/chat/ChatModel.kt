package com.android.kalina.viewmodel.chat

import com.android.kalina.database.message.Message

data class ChatModel(val messages: List<Message> = listOf(), val loading: Boolean = false, val error: Throwable? = null) {

    fun setMessages(messages: List<Message>) = ChatModel(messages, loading, error)

    fun setLoading(loading: Boolean) = ChatModel(messages, loading, error)

    fun setError(error: Throwable?) = ChatModel(messages, loading, error)
}