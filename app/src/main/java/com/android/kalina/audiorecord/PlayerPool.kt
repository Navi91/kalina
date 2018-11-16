package com.android.kalina.audiorecord

import android.content.Context
import com.android.kalina.database.message.Message
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Dmitriy on 08.02.2018.
 */
class PlayerPool {
    private val playerMap = mutableMapOf<Message, AudioPlayer>()
    private val eventPublishSubject = PublishSubject.create<AudioPlayer.PlayerEvent>()
    private var disposable: Disposable? = null

    fun getPlayerById(context: Context, message: Message): AudioPlayer? {
        if (playerMap.containsKey(message)) {
            return playerMap[message]
        }

        try {
            val player = AudioPlayer(message)
            playerMap.put(message, player)

            disposable = player.getEventObservable().subscribe {
                eventPublishSubject.onNext(it)
            }

            return player
        } catch (e: Exception) {
            return null
        }
    }

    fun getPlayerPoolEventObservable() = eventPublishSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())

    fun pause() {
        for (mutableEntry in playerMap) {
            if (mutableEntry.value.isPlay()) {
                mutableEntry.value.pause()
            }
        }
    }

    fun destroy() {
        for (mutableEntry in playerMap) {
            mutableEntry.value.stop()
        }

        disposable?.dispose()
    }

    fun allPaused() = playerMap.none { it.value.isPlay() }
}