package com.android.kalina.ui.activity

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.LinearLayoutManager
import com.jakewharton.rxbinding.widget.RxTextView
import com.android.kalina.R
import com.android.kalina.api.chat.ConnectToServerApiException
import com.android.kalina.api.chat.MessageClientIdGenerator
import com.android.kalina.api.radio.RadioStateController
import com.android.kalina.audiorecord.AudioPlayer
import com.android.kalina.audiorecord.AudioPlayerManager
import com.android.kalina.audiorecord.Recorder
import com.android.kalina.dagger.ComponentHolder
import com.android.kalina.onesignal.MESSAGE_ACTION
import com.android.kalina.ui.KalinaActivity
import com.android.kalina.ui.adapter.MessageRecyclerViewAdapter
import com.android.kalina.ui.view.RecordVoiceView
import com.android.kalina.util.Logger
import com.android.kalina.viewmodel.chat.ActionButtonModel
import com.android.kalina.viewmodel.chat.ChatViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.a_chat.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatActivity : KalinaActivity(), RecordVoiceView.RecordVoiceListener {

    private val TAG = ChatActivity::class.java.name

    private val AUDIO_PERMISSION_REQUEST_CODE = 100

    @Inject
    lateinit var clientIdGenerator: MessageClientIdGenerator
    @Inject
    lateinit var radioStateController: RadioStateController

    private var actionButtonState: ActionButtonModel.State = ActionButtonModel.State.TEXT
    private lateinit var recordVoiceView: RecordVoiceView
    private var timerDisposable: Disposable? = null
    private var eventDisposable: Disposable? = null
    private var stateDisposable: Disposable? = null
    private var recorder: Recorder? = null
    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var viewModel: ChatViewModel
    private var state: Int = PlaybackStateCompat.STATE_NONE
    private var shouldRestoreRadio = false

    private val abortMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) = abortBroadcast()
    }

    companion object {

        fun createOpenChatIntent(context: Context): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.action = Intent.ACTION_MAIN
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ComponentHolder.applicationComponent().inject(this)

        setContentView(R.layout.a_chat)

        audioPlayerManager = AudioPlayerManager(this)

        val size = Point()
        windowManager.defaultDisplay.getSize(size)

        recordVoiceView = RecordVoiceView(this, recordGroup, timeTextView, cancelLayout, actionButton, size.y.toFloat(), 20500)
        recordVoiceView.setTime(0)
        recordVoiceView.recordListener = this

        backImageView.setOnClickListener { onBackPressed() }

        val adapter = MessageRecyclerViewAdapter(audioPlayerManager)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(this@ChatActivity).apply {
            stackFromEnd = true
        }

        recyclerView.apply {
            this.adapter = adapter
            this.layoutManager = layoutManager
        }

        viewModel = ViewModelProviders.of(this).get(ChatViewModel::class.java)
        viewModel.getChatModelLiveData().observe(this, Observer { chatModel ->
            chatModel?.let {
                val needScrollToEnd = layoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1

                adapter.setItems(it.messages)
                adapter.notifyDataSetChanged()
                viewModel.messageRead()

                if (needScrollToEnd) {
                    layoutManager.scrollToPositionWithOffset(adapter.itemCount - 1, 0)
                }

                val error = chatModel.error
                if (error != null) {
                    if (error is ConnectToServerApiException) {
                        showToast(getString(R.string.error_server_unavailable))
                    } else {
                        showToast(error.message)
                    }
                }
            }
        })
        viewModel.getActionButtonData().observe(this, Observer { buttonModel ->
            actionButtonState = buttonModel?.state ?: ActionButtonModel.State.TEXT

            if (actionButtonState == ActionButtonModel.State.TEXT) {
                actionButton.setImageDrawable(VectorDrawableCompat.create(resources, R.drawable.ic_send_message_89dp, theme))
                recordVoiceView.setEnable(false)
                actionButton.setOnClickListener {
                    viewModel.sendTextMessage(messageEditText.text.toString())
                    messageEditText.setText("")
                }
            } else {
                actionButton.setImageDrawable(VectorDrawableCompat.create(resources, R.drawable.ic_mic_89dp, theme))
                actionButton.setOnClickListener {
                    if (permissionDenied()) {
                        requestAudioPermission()
                    }
                }
                recordVoiceView.setEnable(!permissionDenied())
            }
        })

        RxTextView.textChanges(messageEditText).map { it.toString() }.subscribe { viewModel.setText(it) }

        requestPermissionIfNeed()

        stateDisposable = radioStateController.getPlaybackStateObservable().subscribe {
            it?.let {
                state = it

                Logger.log(TAG, "state changed $state")

                if (runState(state) && !audioPlayerManager.allPaused()) {
                    audioPlayerManager.pause()
                }
            }
        }
        eventDisposable = audioPlayerManager.getPlayerPoolEventObservable().subscribe {
            if (it == AudioPlayer.PlayerEvent.PAUSE || it == AudioPlayer.PlayerEvent.COMPLETE) {
                playRadioIfNeed()
                shouldRestoreRadio = false
            } else if (it == AudioPlayer.PlayerEvent.START) {
                shouldRestoreRadio = runState(state)

                stopRadioIfNeed()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        registerAbortBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        audioPlayerManager.pause()
        unregisterAbortBroadcastReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.destroy()
        eventDisposable?.dispose()
        stateDisposable?.dispose()
    }

    private fun runState(state: Int): Boolean {
        return state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_CONNECTING
    }

    private fun playRadioIfNeed() {
        Logger.log(TAG, "playRadioIfNeed $shouldRestoreRadio")

        if (shouldRestoreRadio && recorder?.isRun() != true) {
            radioStateController.play()
        }
    }

    private fun stopRadioIfNeed() {
        Logger.log(TAG, "stopRadioIfNeed $shouldRestoreRadio")

        if (shouldRestoreRadio) {
            radioStateController.pause()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (actionButtonState == ActionButtonModel.State.AUDIO) {
            recordVoiceView.setEnable(!permissionDenied())
        }
    }

    override fun onRecordStart() {
        shouldRestoreRadio = runState(state)

        messageEditText.hint = ""
        messageEditText.clearFocus()

        audioPlayerManager.pause()

        startRecord()

        val interval: Long = 50
        timerDisposable = Observable.interval(interval, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe {
                    recordVoiceView.setTime(it * interval)
                    recorder?.setTime(it * interval)
                }
    }

    override fun onRecordFinish() {
        messageEditText.setHint(R.string.message)

        timerDisposable?.dispose()
        stopRecord()

        val file = recorder?.getAudioFile()
        val fileName = recorder?.fileName

        if (file != null && fileName != null) {
            viewModel.sendAudioMessage(fileName, file)
        }
    }

    override fun onRecordCancel() {
        messageEditText.setHint(R.string.message)

        stopRecord()

        timerDisposable?.dispose()
    }

    private fun startRecord() {
        stopRadioIfNeed()

        recorder = Recorder(this, clientIdGenerator.getNextId(), 1500)
        recorder?.startRecord()
    }

    private fun stopRecord() {
        recorder?.stopRecord()

        playRadioIfNeed()
        shouldRestoreRadio = false
    }

    private fun requestPermissionIfNeed() {
        if (permissionDenied() && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            requestAudioPermission()
        }
    }

    private fun permissionDenied(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            return permission == PackageManager.PERMISSION_DENIED
        }

        return false
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_REQUEST_CODE)
    }

    private fun registerAbortBroadcastReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(MESSAGE_ACTION)
            priority = 1
        }

        registerReceiver(abortMessageReceiver, intentFilter)
    }

    private fun unregisterAbortBroadcastReceiver() {
        unregisterReceiver(abortMessageReceiver)
    }
}