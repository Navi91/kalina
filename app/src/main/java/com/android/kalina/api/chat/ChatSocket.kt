package com.android.kalina.api.chat

import android.arch.lifecycle.MutableLiveData
import com.android.kalina.api.auth.AuthHolder
import com.android.kalina.api.chat.ChatSocket.EventType.*
import com.android.kalina.api.util.Preferences
import com.android.kalina.api.util.Tracer
import com.android.kalina.dagger.ComponentHolder
import io.reactivex.subjects.PublishSubject
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import io.socket.engineio.client.transports.WebSocket
import java.util.*
import javax.inject.Inject

class ChatSocket @Inject constructor(private val authHolder: AuthHolder, private val preferences: Preferences) {

    private val TAG = "chat_socket"

    private val SOCKET_URL = "https://studio.vvdev.ru:8080"

    private var socket: Socket
    private val eventPublishSubject = PublishSubject.create<Event>()
    private var connected = false

    init {
        Tracer.d(TAG, "init")

        ComponentHolder.applicationComponent().inject(this)

        val options = IO.Options()
        options.transports = arrayOf(WebSocket.NAME)

        socket = IO.socket(SOCKET_URL)
    }

    fun connect() {
        Tracer.d(TAG, "connect")

        addSocketEventCallback(GET_INITIAL_DATA)
        addSocketEventCallback(GET_MESSAGES)
        addSocketEventCallback(SEND_MESSAGE)
        addSocketEventCallback(MESSAGE_NOTIFICATION)
        addSocketEventCallback(MESSAGE_WAS_SEND_NOTIFICATION)
        addSocketEventCallback(MESSAGE_READ_NOTIFICATION)
        addSocketEventCallback(CONNECT_ERROR)
        addSocketEventCallback(CONNECT_TIMEOUT)
        addSocketEventCallback(ERROR)

        socket.apply {
            on(CONNECT.event) {
                Tracer.d(TAG, "on connect event")
                connected = true

                createSocketEventCallback(CONNECT).invoke(it)
                emit(GET_INITIAL_DATA.event)
            }
            on(DISCONNECT.event) {
                Tracer.d(TAG, "on disconnect event")

                connected = false

                createSocketEventCallback(DISCONNECT).invoke(it)
            }
            io().on(Manager.EVENT_TRANSPORT) { arrayOfAnys ->
                val transport = arrayOfAnys[0] as Transport

                transport.apply {
                    on(Transport.EVENT_REQUEST_HEADERS) {
                        val deviceCode = authHolder.getDeviceCode()
                        val phone = authHolder.getPhone()
                        val playerId = preferences.getPlayerId()

                        Tracer.d(TAG, "Request headers device code = $deviceCode phone = $phone player id = $playerId")

                        val headers = it[0] as TreeMap<String, List<String>>
                        headers.apply {
                            put("device_code", listOf(deviceCode))
                            put("phone", listOf(phone))
                            put("player_id", listOf(playerId))
                            put("os", listOf("Android"))
                        }
                    }
                    on(Transport.EVENT_CLOSE) {
                        Tracer.d(TAG, "on event close")
                    }
                }
            }

            connect()
        }
    }

    fun disconnect() {
        Tracer.d(TAG, "disconnect")
    }

    fun emitEvent(type: EventType) {
        Tracer.d(TAG, "emitEvent $type")

        emit(type.event)
    }

    fun emitEvent(type: EventType, arg: Any) {
        Tracer.d(TAG, "emitEvent $type")

        emit(type.event, arg)
    }

    private fun addSocketEventCallback(eventType: EventType) {
        socket.on(eventType.event, createSocketEventCallback(eventType))
    }

    private fun createSocketEventCallback(eventType: EventType): (varargs: Any) -> Unit {
        return {
            Tracer.d(TAG, "event $eventType")

            eventPublishSubject.onNext(Event(eventType, it))
//            eventPublishSubject.postValue(Event(eventType, it))
        }
    }

    private fun emit(emit: String) {
        Tracer.d(TAG, "emit $emit")

        if (connected) {
            socket.emit(emit)
        }
    }

    private fun emit(emit: String, arg: Any) {
        Tracer.d(TAG, "emit $emit $arg")

        if (connected) {
            socket.emit(emit, arg)
        }
    }

    fun getEventLiveData() = eventPublishSubject

    data class Event(val type: EventType, val args: Any)

    enum class EventType(val event: String) {
        CONNECT(Socket.EVENT_CONNECT),
        DISCONNECT(Socket.EVENT_DISCONNECT),
        CONNECT_ERROR(Socket.EVENT_CONNECT_ERROR),
        CONNECT_TIMEOUT(Socket.EVENT_CONNECT_TIMEOUT),
        ERROR(Socket.EVENT_ERROR),
        GET_INITIAL_DATA("get_initial_data"),
        GET_MESSAGES("get_messages"),
        SEND_MESSAGE("post_message"),
        MESSAGE_NOTIFICATION("message_notification"),
        MESSAGE_WAS_SEND_NOTIFICATION("message_was_send_notification"),
        MESSAGE_READ_NOTIFICATION("messages_read_notification"),
    }
}