package com.android.kalina.api.radio

import android.app.PendingIntent
import android.content.Intent
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.media.MediaRouter
import com.android.kalina.api.playback.PlaybackManager
import com.android.kalina.api.playback.RadioPlayback
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.ui.activity.RadioActivity
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by Dmitriy on 24.02.2018.
 */
class RadioService : MediaBrowserServiceCompat(), PlaybackManager.PlaybackServiceCallback {

    private val TAG = "radio_service"

    @Inject
    lateinit var radioStateController: RadioStateController

    private lateinit var playbackManager: PlaybackManager
    private lateinit var session: MediaSessionCompat
    private lateinit var radioNotificationManager: RadioNotificationManager
    private lateinit var mediaRouter: MediaRouter
    private var disposable: Disposable? = null
    private val delayedStopHandler = DelayedStopHandler(this)

    companion object {
        const val MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__"
        const val MEDIA_ID_ROOT = "__ROOT__"

        const val ACTION_CMD = "com.example.android.uamp.ACTION_CMD"
        const val CMD_NAME = "CMD_NAME"
        const val CMD_PAUSE = "CMD_PAUSE"

        private const val STOP_DELAY = 30000
    }

    override fun onCreate() {
        super.onCreate()

        ComponentHolder.applicationComponent().inject(this)

        val playback = RadioPlayback(applicationContext, "http://icecast-studio21.cdnvideo.ru/KalynaK_1a")
        playbackManager = PlaybackManager(playback, this)

        session = MediaSessionCompat(applicationContext, "RadioService")
        sessionToken = session.sessionToken
        session.setCallback(playbackManager.mediaSessionCallback)
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        val intent = Intent(this, RadioActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        session.setSessionActivity(pendingIntent)

        radioNotificationManager = RadioNotificationManager(this)
        mediaRouter = MediaRouter.getInstance(applicationContext)

        playbackManager.updatePlaybackState(null)

        disposable = radioStateController.getRequestStateChangeObservable().subscribe {
            if (it == PlaybackStateCompat.ACTION_PLAY) {
                playbackManager.handlePlayRequest()
            } else if (it == PlaybackStateCompat.ACTION_PAUSE) {
                playbackManager.handlePauseRequest()
            }
        }
    }

    override fun onStartCommand(startIntent: Intent?, flags: Int, startId: Int): Int {
        if (startIntent != null) {
            val action = startIntent.getAction()
            val command = startIntent.getStringExtra(CMD_NAME)
            if (ACTION_CMD == action) {
                if (CMD_PAUSE == command) {
                    playbackManager.handlePauseRequest()
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(session, startIntent)
            }
        }

        delayedStopHandler.removeCallbacksAndMessages(null)
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY.toLong())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        playbackManager.handleStopRequest(null)
        radioNotificationManager.stopNotification()

        delayedStopHandler.removeCallbacksAndMessages(null)
        session.release()
        disposable?.dispose()
    }

    override fun onPlaybackStart() {
        session.isActive = true

        delayedStopHandler.removeCallbacksAndMessages(null)
        startService(Intent(applicationContext, RadioService::class.java))
    }

    override fun onNotificationRequired() {
        radioNotificationManager.startNotification()
    }

    override fun onPlaybackStop() {
        session.isActive = false

        delayedStopHandler.removeCallbacksAndMessages(null)
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY.toLong())

        stopForeground(true)
    }

    override fun onPlaybackStateUpdated(state: PlaybackStateCompat) {
        session.setPlaybackState(state)
        radioStateController.setState(state.state)
    }

    override fun onPlaybackMetadataUpdated(metadata: MediaMetadataCompat) {
        session.setMetadata(metadata)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {

    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? = BrowserRoot(MEDIA_ID_ROOT, null)

    private class DelayedStopHandler (service: RadioService) : Handler() {
        private val weakReference: WeakReference<RadioService> = WeakReference<RadioService>(service)

        override fun handleMessage(msg: Message) {
            val service = weakReference.get()
            if (service != null) {
                if (service.playbackManager.playback.isPlaying) {
                    return
                }

                service.stopSelf()
            }
        }
    }
}