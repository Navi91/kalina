package com.android.kalina.audiorecord

import android.content.Context
import com.android.kalina.database.message.Message

/**
 * Created by Dmitriy on 08.02.2018.
 */
class AudioPlayerManager(val context: Context) {

    private val playerPool = PlayerPool()
    private var currentPlayer: AudioPlayer? = null

    fun requestPlay(message: Message) {
        currentPlayer?.pause()

        currentPlayer = getPlayerById(message)
        currentPlayer?.start()
    }

    fun requestPause(message: Message) {
        val player = getPlayerById(message)
        if (player != null && player.isPlay()) {
            player.pause()
        }
    }

    fun pause() {
        playerPool.pause()
    }

    fun destroy() {
        playerPool.destroy()
    }

    fun getPlayerPoolEventObservable() = playerPool.getPlayerPoolEventObservable()

    fun allPaused() = playerPool.allPaused()

    fun getPlayerById(message: Message) = playerPool.getPlayerById(context, message)
}