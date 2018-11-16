package com.android.kalina.onesignal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.android.kalina.R
import com.android.kalina.api.auth.AuthHolder
import com.android.kalina.api.chat.MessageRepository
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.ui.activity.ChatActivity
import com.android.kalina.util.Logger
import javax.inject.Inject

/**
 * Created by Dmitriy on 03.02.2018.
 */

const val MESSAGE_ACTION = "studio21_one_signal_message_notification_action"

class MessageReceiver : BroadcastReceiver() {

    private val TAG = MessageReceiver::class.java.name

    @Inject
    lateinit var authHolder: AuthHolder
    @Inject
    lateinit var messageRepository: MessageRepository

    companion object {
        val TITLE_EXTRA = "extra_title"
        val BODY_EXTRA = "body_title"

        private val MESSAGE_CHANNEL_ID = "message_channel_id"

        var messageId = 100
    }

    init {
        ComponentHolder.applicationComponent().inject(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!authHolder.isAuth()) return

        messageRepository.incrementUnreadMessageCount()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context)
        }

        val title = intent.getStringExtra(TITLE_EXTRA)
        val body = intent.getStringExtra(BODY_EXTRA)

        Logger.log(TAG, "onReceive $title $body")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val chatIntent = ChatActivity.createOpenChatIntent(context)

        val pendingIntent = PendingIntent.getActivity(context, 0, chatIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(intent.getStringExtra(TITLE_EXTRA))
                .setStyle(NotificationCompat.BigTextStyle())
                .setContentText(intent.getStringExtra(BODY_EXTRA))
                .setContentIntent(pendingIntent)
//                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setDefaults(NotificationCompat.DEFAULT_SOUND.or(NotificationCompat.DEFAULT_LIGHTS).or(NotificationCompat.DEFAULT_VIBRATE))
                .setAutoCancel(true)

        notificationManager.notify(messageId, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(MESSAGE_CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(MESSAGE_CHANNEL_ID,
                    context.getString(R.string.message_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH)

            notificationChannel.description = context.getString(R.string.message_notification_channel_description)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}