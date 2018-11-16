package com.android.kalina.ui.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import com.android.kalina.R
import com.android.kalina.api.ArtLoadRequest
import com.android.kalina.api.ArtLoader
import com.android.kalina.api.auth.AuthHolder
import com.android.kalina.api.chat.MessageRepository
import com.android.kalina.api.playback.PlaybackManager
import com.android.kalina.api.util.Preferences
import com.android.kalina.api.util.observeMain
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.ui.Studio21Fragment
import com.android.kalina.ui.activity.AuthActivity
import com.android.kalina.ui.activity.ChatActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.f_radio.*
import javax.inject.Inject


class RadioFragment : Studio21Fragment() {

    @Inject
    lateinit var preferences: Preferences
    @Inject
    lateinit var authHolder: AuthHolder
    @Inject
    lateinit var messageRepository: MessageRepository

    private var artUrl: String? = null
    private lateinit var artLoader: ArtLoader
    private lateinit var audioManager: AudioManager
    private var loadImageRequest: ArtLoadRequest? = null
    private var unreadMessageDisposable: Disposable? = null

    private val callback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            this@RadioFragment.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            this@RadioFragment.onMetadataChanged(metadata)
        }
    }

    companion object {
        fun newInstance(): RadioFragment {
            val fragment = RadioFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ComponentHolder.applicationComponent().inject(this)

        artLoader = ArtLoader(activity!!)
        audioManager = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        ComponentHolder.applicationComponent().inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.f_radio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typeface = Typeface.createFromAsset(activity!!.assets, "Montserrat-SemiBold.ttf")
        titleTextView.setTypeface(typeface, Typeface.NORMAL)
        subtitleTextView.setTypeface(typeface, Typeface.NORMAL)

        invalidateVolumeSeekBar()

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferences.setVolume(convertProgressToFloat(progress))
                setVolumeMedia()
            }
        })

        actionImageView.setOnClickListener { onActionClicked() }
        chatImageView.setOnClickListener { onChatClicked() }

        unreadMessageDisposable = messageRepository.getUnreadMessageCountObservable()
                .observeMain()
                .subscribe {
                    if (it == 0) {
                        chatImageView.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_chat_icon_24dp))
                    } else {
                        chatImageView.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_unread_message_24dp))
                    }
                }
    }

    private fun invalidateVolumeSeekBar() {
        volumeSeekBar.progress = convertProgressFromFloat(preferences.getVolume())
    }

    override fun onStart() {
        super.onStart()

        val controller = MediaControllerCompat.getMediaController(activity!!)
        if (controller != null) {
            onConnected()
        }
    }

    override fun onStop() {
        super.onStop()

        val controller = MediaControllerCompat.getMediaController(activity!!)
        controller?.unregisterCallback(callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        unsubscribeLoadRequest()
        unreadMessageDisposable?.dispose()
    }

    fun onActionClicked() {
        val controller = MediaControllerCompat.getMediaController(activity!!)
        val stateObj = controller.playbackState
        val state = stateObj?.state ?: PlaybackStateCompat.STATE_NONE

        if (state == PlaybackStateCompat.STATE_PAUSED ||
                state == PlaybackStateCompat.STATE_STOPPED ||
                state == PlaybackStateCompat.STATE_NONE) {
            playMedia()
        } else if (state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING) {
            pauseMedia()
        }
    }

    fun onChatClicked() {
        val intent = if (authHolder.isAuth()) {
            Intent(activity, ChatActivity::class.java)
        } else {
            Intent(activity, AuthActivity::class.java)
        }
        activity?.startActivity(intent)
    }

    fun onConnected() {
        val controller = MediaControllerCompat.getMediaController(activity!!)

        if (controller != null) {
            onMetadataChanged(controller.metadata)
            onPlaybackStateChanged(controller.playbackState)
            controller.registerCallback(callback)
        }
    }

    private fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        if (activity == null || state == null) {
            return
        }

        var enablePlay = false
        when (state.state) {
            PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.STATE_NONE -> enablePlay = true
            PlaybackStateCompat.STATE_ERROR -> {
                Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show()
                pauseMedia()
            }
        }

        if (enablePlay) {
            actionImageView.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_play_accent_big))
        } else {
            actionImageView.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_pause_accent_big))
        }
    }

    private fun convertProgressFromFloat(progress: Float): Int = (progress * 100).toInt()

    private fun convertProgressToFloat(progress: Int): Float = progress.toFloat() / 100

    private fun subscribeLoadRequest() {
        loadImageRequest?.subscribe()
    }

    private fun unsubscribeLoadRequest() {
        loadImageRequest?.unsubscribe()
    }

    private fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        if (activity == null || metadata == null) {
            return
        }

        val author = metadata.description.title
        val song = metadata.description.subtitle

        if (TextUtils.isEmpty(author) || TextUtils.isEmpty(song)) return
        unsubscribeLoadRequest()

        titleTextView.text = author
        subtitleTextView.text = song


        loadArtImage(author.toString(), song.toString())
    }

    private fun loadArtImage(author: String, song: String) {
        loadImageRequest = artLoader.request(author, song, object : ArtLoadRequest.LoadCallback {
            override fun onLoad(bitmap: Bitmap?) {
                if (bitmap != null) {
                    activity?.runOnUiThread({
                        artImageView.setImageBitmap(bitmap)
                    })
                }
            }
        })

        artImageView.setImageDrawable(ContextCompat.getDrawable(activity!!, R.drawable.ic_logo_gray))
        subscribeLoadRequest()
    }

    private fun playMedia() {
        val controller = MediaControllerCompat.getMediaController(activity!!)
        controller?.transportControls?.play()
    }

    private fun pauseMedia() {
        val controller = MediaControllerCompat.getMediaController(activity!!)
        controller?.transportControls?.pause()
    }

    private fun setVolumeMedia() {
        val controller = MediaControllerCompat.getMediaController(activity!!)
        controller?.transportControls?.sendCustomAction(PlaybackManager.VOLUME_CUSTOM_ACTION, PlaybackManager.createVolumeArgs(preferences.getVolume()))
    }
}
