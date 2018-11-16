package com.android.kalina.viewmodel.chat

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import com.android.kalina.api.chat.ChatApi
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.data.LiveResource
import com.android.kalina.data.Status
import com.android.kalina.database.message.Message
import com.android.kalina.viewmodel.KalinaViewModel
import java.io.File
import javax.inject.Inject

class ChatViewModel : KalinaViewModel() {

    @Inject
    lateinit var chatApi: ChatApi

    private val chatModelLiveData = MediatorLiveData<ChatModel>()
    private val messagesRepositoryData: LiveData<LiveResource<List<Message>>>
    private val actionButtonData = MutableLiveData<ActionButtonModel>()

    init {
        ComponentHolder.applicationComponent().inject(this)

        chatModelLiveData.value = ChatModel()
        actionButtonData.value = ActionButtonModel(ActionButtonModel.State.TEXT)
        messagesRepositoryData = chatApi.getMessagesLiveData()

        chatModelLiveData.addSource(messagesRepositoryData) { resource ->
            when (resource?.status) {
                Status.SUCCESS -> {
                    val value = chatModelLiveData.value
                    chatModelLiveData.value = value?.setError(null)?.setLoading(false)?.setMessages(resource.data
                            ?: listOf())
                }
                Status.LOADING -> {
                    val value = chatModelLiveData.value
                    chatModelLiveData.value = value?.setError(null)?.setLoading(true)?.setMessages(resource.data
                            ?: listOf())
                }
                Status.ERROR -> {
                    val value = chatModelLiveData.value
                    chatModelLiveData.value = value?.setError(resource.error)?.setLoading(false)?.setMessages(resource.data
                            ?: listOf())
                }
            }
        }

    }

    fun messageRead() {
        chatApi.messageRead()
    }

    override fun onCleared() {
        super.onCleared()

        chatModelLiveData.removeSource(messagesRepositoryData)
    }

    fun setText(text: String) {
        actionButtonData.value = ActionButtonModel(if (text.isNotEmpty()) ActionButtonModel.State.TEXT else ActionButtonModel.State.AUDIO)
    }

    fun sendTextMessage(text: String) {
        chatApi.sendTextMessage(text)
    }

    fun sendAudioMessage(clientMessageId: String, file: File) {
        chatApi.sendAudioMessage(clientMessageId, file)
    }

    fun getChatModelLiveData() = chatModelLiveData

    fun getActionButtonData() = actionButtonData
}