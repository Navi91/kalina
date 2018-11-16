package com.android.kalina.dagger

import com.android.kalina.api.chat.ChatSocket
import com.android.kalina.api.playback.RadioPlayback
import com.android.kalina.api.radio.RadioService
import com.android.kalina.app.Studio21Application
import com.android.kalina.audiorecord.AudioApi
import com.android.kalina.audiorecord.AudioPlayer
import com.android.kalina.onesignal.MessageReceiver
import com.android.kalina.ui.activity.ChatActivity
import com.android.kalina.ui.fragment.RadioFragment
import com.android.kalina.viewmodel.auth.AuthViewModel
import com.android.kalina.viewmodel.chat.ChatViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, Studio21Module::class])
interface ApplicationComponent {

    fun inject(radioFragment: RadioFragment)
    fun inject(radioPlayback: RadioPlayback)
    fun inject(authViewModel: AuthViewModel)
    fun inject(chatViewModel: ChatViewModel)
    fun inject(chatSocket: ChatSocket)
    fun inject(audioApi: AudioApi)
    fun inject(chatActivity: ChatActivity)
    fun inject(radioService: RadioService)
    fun inject(audioPlayer: AudioPlayer)
    fun inject(studio21Application: Studio21Application)
    fun inject(messageReceiver: MessageReceiver)
}