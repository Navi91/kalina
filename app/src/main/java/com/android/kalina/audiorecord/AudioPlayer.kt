package com.android.kalina.audiorecord

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.android.kalina.api.util.io
import com.android.kalina.api.util.observeMain
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.database.message.Message
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Dmitriy on 08.02.2018.
 */
class AudioPlayer constructor(val message: Message) {

    @Inject
    lateinit var context: Context

    private val player = MediaPlayer()
    private val eventPublishSubject = PublishSubject.create<PlayerEvent>()
    private var play = false
    private var created = false
    private var disposable: Disposable? = null

    init {
        ComponentHolder.applicationComponent().inject(this)

        create()
    }

    private fun create() {
        val source = if (message.isIncome()) {
            message.audio
        } else {
            AudioHelper.createFileName(context, message.client_id)
        }
        player.setDataSource(source)
        player.prepare()

        player.setOnCompletionListener {
            eventPublishSubject.onNext(PlayerEvent.COMPLETE)
        }

        created = true
    }

    fun start() {
        player.start()

        disposable = Observable.interval(50, TimeUnit.MILLISECONDS)
                .io()
                .observeMain()
                .subscribe {
                    if (isPlay()) {
                        eventPublishSubject.onNext(PlayerEvent.DURATION)
                    }
                }

        play = true

        eventPublishSubject.onNext(PlayerEvent.START)
        Log.d("player_trace", "Duration ")
    }

    fun pause() {
        dispose()

        eventPublishSubject.onNext(PlayerEvent.PAUSE)
        player.pause()

        play = false
    }

    fun stop() {
        dispose()

        if (created) {
            player.stop()
            player.release()
            play = false
        }
    }

    fun getCurrentPosition() = player.currentPosition

    fun getDuration() = player.duration

    private fun dispose() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable?.dispose()
        }
    }

    fun isPlay() = player.isPlaying

    fun getEventObservable() = eventPublishSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())

    enum class PlayerEvent {
        START, PAUSE, DURATION, COMPLETE
    }
}