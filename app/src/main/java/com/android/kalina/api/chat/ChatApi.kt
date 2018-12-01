package com.android.kalina.api.chat

import android.arch.lifecycle.MediatorLiveData
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.google.gson.GsonBuilder
import com.android.kalina.api.chat.ChatSocket.EventType.*
import com.android.kalina.api.util.Preferences
import com.android.kalina.api.util.Tracer
import com.android.kalina.api.util.io
import com.android.kalina.audiorecord.AudioApi
import com.android.kalina.audiorecord.AudioHelper
import com.android.kalina.data.LiveResource
import com.android.kalina.data.Status
import com.android.kalina.database.message.Message
import io.reactivex.Completable
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatApi @Inject constructor(private val context: Context,
                                  private val messageRepository: MessageRepository,
                                  private val chatSocket: ChatSocket,
                                  private val audioApi: AudioApi,
                                  private val preferences: Preferences,
                                  private val clientIdGenerator: MessageClientIdGenerator) {

    private val TAG = "chat_api"

    private val messagesLiveData = MediatorLiveData<LiveResource<List<Message>>>()
    private val gson = GsonBuilder().create()

    init {
        Tracer.d(TAG, "init")

        messagesLiveData.addSource(messageRepository.getAllMessageLiveData()) { list ->
            list?.let {
                messagesLiveData.value = LiveResource.success(it)
            }
        }
        chatSocket.getEventLiveData().subscribe {
            onSocketEvent(it)
        }
        chatSocket.connect()
    }

    private fun requestMessages() {
        Tracer.d(TAG, "requestMessages")

        getMessageFromLastId(preferences.getLastMessageId())
    }

    private fun getMessageFromLastId(id: Long) {
        val json = JSONObject()
        json.put("last_message_id", id)

        emitSocketEvent(GET_MESSAGES, json)
    }

    fun sendTextMessage(text: String) {
        Tracer.d(TAG, "sendTextMessage $text")

        Completable.fromCallable {
            val createdAt = TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().timeInMillis)
            val clientId = clientIdGenerator.getNextId()
            val message = Message.createUnsentTextMessage(text, clientId, createdAt)

            addMessageToRepo(message)
            sendMessage(message)
        }.io().subscribe()
    }

    private fun addMessageToRepo(message: Message) {
        messageRepository.addMessage(listOf(message))
    }

    fun sendAudioMessage(clientMessageId: String, file: File) {
        Tracer.d(TAG, "sendAudioMessage $clientMessageId $file")

        Completable.fromCallable {
            val createdAt = TimeUnit.MILLISECONDS.toSeconds(Calendar.getInstance().timeInMillis)
            val message = Message.createUnsentAudioMessage("", clientMessageId, createdAt)

            messageRepository.addMessage(listOf(message))

            uploadAudioAndSendMessage(file, clientMessageId, createdAt)
        }.io().subscribe()
    }

    private fun sendMessage(message: Message) {
        Tracer.d(TAG, "sendMessage $message")

        val json = createMessageJson(message)
        emitSocketEvent(SEND_MESSAGE, json)
    }

    private fun uploadAudioAndSendMessage(file: File, clientMessageId: String, createdAt: Long) {
        audioApi.uploadFile(file, {
            Completable.fromCallable {
                val message = Message.createUnsentAudioMessage(it, clientMessageId, createdAt)
                messageRepository.addMessage(listOf(message))

                sendMessage(message)
            }.io().subscribe()
        }, {
        })
    }

    private fun onSocketEvent(event: ChatSocket.Event) {
        Tracer.d(TAG, "onSocketEvent $event")

        when (event.type) {
            GET_INITIAL_DATA -> {
                handleInitialData(event.args)
            }
            GET_MESSAGES -> {
                handleMessages(event.args)
            }
            MESSAGE_NOTIFICATION -> {
                handleMessageNotification(event.args)
            }
            MESSAGE_WAS_SEND_NOTIFICATION -> {
                handleMessageNotificationWasSent(event.args)
            }
            MESSAGE_READ_NOTIFICATION -> {
                setUnreadMessageCount(0)
            }
            CONNECT_ERROR -> {
                setError(ConnectToServerApiException())
            }
            else -> {
            }
        }
    }

    private fun handleInitialData(vararg args: Any) {
        Tracer.d(TAG, "handleInitialData $args")

        val objects: Array<Any> = args[0] as Array<Any>
        val json = objects[0] as JSONObject

        Log.d(TAG, json.toString())

        val status = json.getString("status")
        val ok = TextUtils.equals(status, "ok")

        if (ok) {
            requestMessages()
            sendNextUnsentMessageIfExist()
        } else {
            setError(ConnectToServerApiException())
        }

        Log.d(TAG, "Get initial: $ok")
    }

    private fun handleMessages(vararg args: Any) {
        Completable.fromCallable {
            val objects: Array<Any> = args[0] as Array<Any>
            val json = objects[0] as JSONObject
            val array = json.getJSONArray("messages")
            val unreadCount = json.getInt("unread_count")

            Tracer.d(TAG, "handleMessages $json")

            setUnreadMessageCount(unreadCount)

            val messages = mutableListOf<Message>()
            var lastMessageId = preferences.getLastMessageId()

            for (index in 0 until array.length()) {

                val message = gson.fromJson(array.getJSONObject(index).toString(), Message::class.java)
                if (!message.isIncome()) messageRepository.deleteByClientId(message.client_id)

                if (lastMessageId < message.id) {
                    lastMessageId = message.id
                }
                messages.add(message)
            }

            preferences.setLastMessageId(lastMessageId)

            messageRepository.addMessage(messages)
        }.io().subscribe()
    }

    private fun handleMessageNotification(vararg args: Any) {
        Completable.fromCallable {
            val objects = args[0] as Array<Any>
            val json = objects[0] as JSONObject
            val message = gson.fromJson(json.toString(), Message::class.java)

            Tracer.d(TAG, "handleMessageNotification $json")

            if (message.isIncome()) {
                incrementUnreadMessageCount()
                messageRepository.addMessage(listOf(message))
            } else {
                messageRepository.deleteByClientId(message.client_id)
                messageRepository.addMessage(listOf(message))
//                messageRepository.updateMessageIdByClientId(message.id, message.client_id)
            }

            if (preferences.getLastMessageId() < message.prev_id) {
                requestMessages()
            }

            sendNextUnsentMessageIfExist()
        }.io().subscribe()
    }

    private fun handleMessageNotificationWasSent(vararg args: Any) {
        Completable.fromCallable {
            val objects = args[0] as Array<Any>
            val json = objects[0] as JSONObject

            Tracer.d(TAG, "message was send notification $json")

//            messageRepository.updateMessageIdByClientId(json.optLong("id"), json.optString("client_id"))
            sendNextUnsentMessageIfExist()
        }.io().subscribe()
    }

    private fun sendNextUnsentMessageIfExist() {
        Tracer.d(TAG, "sendNextUnsentMessageIfExist")

        Completable.fromCallable {
            val unsentMessages = messageRepository.getAllUnsentMessage()
            val message = unsentMessages.firstOrNull()

            if (message != null) {
                if (message.isAudio()) {
                    val file = AudioHelper.getAudioFile(context, message.client_id)

                    if (file != null) {
                        uploadAudioAndSendMessage(file, message.client_id, message.created_at)
                    } else {
                        sendMessage(message)
                    }
                } else {
                    sendMessage(message)
                }
            }
        }.io().subscribe()
    }

    fun getMessagesLiveData() = messagesLiveData

    private fun emitSocketEvent(type: ChatSocket.EventType, arg: Any) {
        Tracer.d(TAG, "emitSocketEvent $type")

        chatSocket.emitEvent(type, arg)
    }

    private fun createMessageJson(message: Message): JSONObject {
        val json = JSONObject()
        json.put("text", message.text)
        json.put("audio", message.audio)
        json.put("client_id", message.client_id)

        return json
    }

    fun messageRead() {
        chatSocket.emitEvent(MESSAGE_READ_NOTIFICATION)
    }

    private fun incrementUnreadMessageCount() {
        messageRepository.incrementUnreadMessageCount()
    }

    private fun setUnreadMessageCount(count: Int) {
        messageRepository.setUnreadMessageCount(count)
    }

    private fun setError(error: Throwable) {
        if (messagesLiveData.value?.status != Status.ERROR) {
            val value = messagesLiveData.value
            messagesLiveData.postValue(LiveResource.error(error, value?.data))
        }
    }
}