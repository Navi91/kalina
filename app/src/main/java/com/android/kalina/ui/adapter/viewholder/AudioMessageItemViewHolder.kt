package com.android.kalina.ui.adapter.viewholder

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.android.kalina.R
import com.android.kalina.api.util.getTimeFormatString
import com.android.kalina.api.util.setVisible
import com.android.kalina.audiorecord.AudioPlayer
import com.android.kalina.audiorecord.AudioPlayerManager
import com.android.kalina.database.message.Message
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.v_out_audio_message_item.view.*
import java.text.SimpleDateFormat
import java.util.*

class AudioMessageItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var disposable: Disposable? = null
    private val durationFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    fun bind(message: Message, playerManager: AudioPlayerManager) {
        itemView.timeTextView.text = message.getCreateTimeInMilliseconds().getTimeFormatString()
        itemView.progressImageView?.setVisible(message.isUnsent())
        itemView.progressBar.setVisible(!message.isUnsent())

        itemView.actionButton.setOnClickListener {
            val player = playerManager.getPlayerById(message)

            if (player != null) {
                if (player.isPlay()) {
                    playerManager.requestPause(message)
                } else {
                    playerManager.requestPlay(message)
                }
            } else {
                Toast.makeText(itemView.context, R.string.error_file_not_found, Toast.LENGTH_SHORT).show()
            }
        }

        val player = playerManager.getPlayerById(message)
        bindPlayer(player)

        dispose()

        if (player != null) {
            disposable = player.getEventObservable().subscribe {
                bindPlayer(playerManager.getPlayerById(message))

                if (it == AudioPlayer.PlayerEvent.COMPLETE) {
                    itemView.progressBar.progress = 0
                    itemView.durationTextView.text = durationFormat.format(0)
                }
            }
        }
    }

    private fun bindPlayer(player: AudioPlayer?) = if (player != null) {
        val resourceId = if (player.isPlay()) R.drawable.ic_audio_message_pause_89dp else R.drawable.ic_audio_message_play_89dp
        itemView.actionButton.setImageDrawable(ContextCompat.getDrawable(itemView.context, resourceId))

        val duration = player.getDuration().toFloat()
        val current = player.getCurrentPosition().toFloat()

        itemView.progressBar.progress = (current * 100F / duration).toInt()
        itemView.durationTextView.text = durationFormat.format(current)
    } else {
        itemView.progressBar.progress = 0
        itemView.durationTextView.text = durationFormat.format(0)
    }

    private fun dispose() {
        if (disposable != null && !disposable!!.isDisposed) {
            disposable?.dispose()
        }
    }

    fun onRecycled() {
        dispose()
    }

}