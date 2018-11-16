package com.android.kalina.dagger.api

import android.content.Context
import com.android.kalina.api.auth.AuthHolder
import com.android.kalina.api.chat.ChatApi
import com.android.kalina.api.chat.ChatSocket
import com.android.kalina.api.chat.MessageClientIdGenerator
import com.android.kalina.api.chat.MessageRepository
import com.android.kalina.api.util.Preferences
import com.android.kalina.audiorecord.AudioApi
import com.android.kalina.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Singleton
class ChatModule {

    @Provides
    @Singleton
    fun provideMessageRepository(appDatabase: AppDatabase, preferences: Preferences): MessageRepository {
        return MessageRepository(appDatabase, preferences)
    }

    @Provides
    @Singleton
    fun provideChatSocket(authHolder: AuthHolder, preferences: Preferences): ChatSocket {
        return ChatSocket(authHolder, preferences)
    }

    @Provides
    fun provideAudioApy(context: Context) = AudioApi(context)

    @Provides
    @Singleton
    fun provideChatApi(context: Context, messageRepository: MessageRepository, chatSocket: ChatSocket, audioApi: AudioApi, preferences: Preferences, generator: MessageClientIdGenerator): ChatApi {
        return ChatApi(context, messageRepository, chatSocket, audioApi, preferences, generator)
    }

    @Singleton
    @Provides
    fun provideMessageClientIdGenerator(preferences: Preferences) = MessageClientIdGenerator(preferences)
}