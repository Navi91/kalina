package com.android.kalina.api.radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.RemoteException
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import com.android.kalina.R
import com.android.kalina.api.ArtLoadRequest
import com.android.kalina.api.ArtLoader
import com.android.kalina.util.Logger
import com.android.kalina.ui.activity.RadioActivity

/**
 * Created by Dmitriy on 25.02.2018.
 */
class RadioNotificationManager(val service: RadioService) : BroadcastReceiver() {

    private val TAG = "radio_notification_manager"

    private var sessionToken: MediaSessionCompat.Token? = null
    private var controller: MediaControllerCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    private var playbackState: PlaybackStateCompat? = null
    private var metadata: MediaMetadataCompat? = null
    private val artLoader = ArtLoader(service)
    private var artLoadRequest: ArtLoadRequest? = null

    private val notificationManager: NotificationManager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val pkg = service.packageName
    private val playIntent: PendingIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
            Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    private val pauseIntent: PendingIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
            Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)
    private val stopIntent: PendingIntent = PendingIntent.getBroadcast(service, REQUEST_CODE,
            Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT)

    private var started = false

    companion object {
        private val CHANNEL_ID = "radio_channel_id"

        private val NOTIFICATION_ID = 412
        private val REQUEST_CODE = 100

        const val ACTION_PAUSE = "action_pause"
        const val ACTION_PLAY = "action_play"
        const val ACTION_STOP = "action_stop"
    }

    init {
        updateSessionToken()
        notificationManager.cancelAll()
    }

    fun startNotification() {
        Logger.log(TAG, "startNotification started: $started")

        if (!started) {
            metadata = controller?.metadata
            playbackState = controller?.playbackState

            // The notification must be updated after setting started to true
            val notification = createNotification()
            if (notification != null) {
                controller?.registerCallback(mediaControllerCallback)
                val filter = IntentFilter()
                filter.addAction(ACTION_PAUSE)
                filter.addAction(ACTION_PLAY)
                filter.addAction(ACTION_STOP)
                service.registerReceiver(this, filter)

                service.startForeground(NOTIFICATION_ID, notification)
                started = true
            }
        }
    }

    fun stopNotification() {
        if (started) {
            started = false
            controller?.unregisterCallback(mediaControllerCallback)
            try {
                unsubscribeLoadRequest()
                notificationManager.cancel(NOTIFICATION_ID)
                service.unregisterReceiver(this)
            } catch (ex: IllegalArgumentException) {
                // ignore if the receiver is not registered.
            }

            service.stopForeground(true)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        Logger.log(TAG, "onReceive action $action")

        when (action) {
            ACTION_PAUSE -> transportControls?.pause()
            ACTION_PLAY -> transportControls?.play()
            ACTION_STOP -> transportControls?.stop()
            else -> Logger.log(TAG, "Unknown intent ignored. Action=$action")
        }
    }

    private fun createContentIntent(description: MediaDescriptionCompat?): PendingIntent {
        val openUI = Intent(service, RadioActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//        openUI.putExtra(MusicPlayerActivity.EXTRA_START_FULLSCREEN, true)

        if (description != null) {
//            openUI.putExtra(MusicPlayerActivity.EXTRA_CURRENT_MEDIA_DESCRIPTION, description)
        }

        return PendingIntent.getActivity(service, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            this@RadioNotificationManager.playbackState = state
            if (state.state == PlaybackStateCompat.STATE_STOPPED || state.state == PlaybackStateCompat.STATE_NONE) {
                stopNotification()
            } else {
                val notification = createNotification()
                if (notification != null) {
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            this@RadioNotificationManager.metadata = metadata
            val notification = createNotification()
            if (notification != null) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            try {
                updateSessionToken()
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(RemoteException::class)
    private fun updateSessionToken() {
        val freshToken = service.getSessionToken()
        if (sessionToken == null && freshToken != null || sessionToken != null && sessionToken != freshToken) {
            controller?.unregisterCallback(mediaControllerCallback)

            sessionToken = freshToken
            if (sessionToken != null) {
                controller = MediaControllerCompat(service, sessionToken!!)
                transportControls = controller?.transportControls

                if (started) {
                    controller?.registerCallback(mediaControllerCallback)
                }
            }
        }
    }

    private fun createNotification(): Notification? {
        if (metadata == null || playbackState == null) {
            return null
        }

        val description = metadata?.description ?: return null

        val author = description.title
        val song = description.subtitle

        if (TextUtils.isEmpty(author) || TextUtils.isEmpty(song)) return null

        unsubscribeLoadRequest()

        val art: Bitmap = BitmapFactory.decodeResource(service.resources, R.mipmap.ic_notification_placeholder)

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationBuilder = NotificationCompat.Builder(service, CHANNEL_ID)

        addActions(notificationBuilder)
        notificationBuilder
                .setStyle(MediaStyle()
                        // show only play/pause in compact view
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopIntent)
                        .setMediaSession(sessionToken))
                .setDeleteIntent(stopIntent)
//                .setColor(ContextCompat.getColor(service, android.R.color.black))
                .setSmallIcon(R.mipmap.ic_notification)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent(description))
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setLargeIcon(art)

        setNotificationPlaybackState(notificationBuilder)
        fetchBitmapFromURLAsync(author.toString(), song.toString(), notificationBuilder)

        return notificationBuilder.build()
    }

    private fun addActions(notificationBuilder: NotificationCompat.Builder) {
        val label: String
        val icon: Int
        val intent: PendingIntent

        if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
            label = service.getString(R.string.pause)
            icon = R.drawable.ic_pause_black_24dp
            intent = pauseIntent
        } else {
            label = service.getString(R.string.play)
            icon = R.drawable.ic_play_arrow_black_24dp
            intent = playIntent
        }

        notificationBuilder.addAction(NotificationCompat.Action(icon, label, intent))
    }

    private fun subscribeLoadRequest() {
        artLoadRequest?.subscribe()
    }

    private fun unsubscribeLoadRequest() {
        artLoadRequest?.unsubscribe()
    }

    private fun setNotificationPlaybackState(builder: NotificationCompat.Builder) {
        if (playbackState == null || !started) {
            service.stopForeground(true)
            return
        }

        builder.setOngoing(playbackState?.state == PlaybackStateCompat.STATE_PLAYING)
    }

    private fun fetchBitmapFromURLAsync(author: String, song: String, builder: NotificationCompat.Builder) {

        artLoadRequest = artLoader.request(author, song, object : ArtLoadRequest.LoadCallback {
            override fun onLoad(bitmap: Bitmap?) {
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap)
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                }
            }
        })
        subscribeLoadRequest()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    service.getString(R.string.radio_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW)

            notificationChannel.description = service.getString(R.string.radio_notification_channel_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}