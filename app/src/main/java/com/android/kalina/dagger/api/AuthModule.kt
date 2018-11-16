package com.android.kalina.dagger.api

import com.android.kalina.api.auth.AuthApi
import com.android.kalina.api.auth.AuthHolder
import com.android.kalina.api.retrofit.RetrofitApi
import com.android.kalina.api.util.Preferences
import dagger.Module
import dagger.Provides

@Module
class AuthModule {

    @Provides
    fun provideAuthHolder(preferences: Preferences) = AuthHolder(preferences)

    @Provides
    fun provideAuthApi(retrofitApi: RetrofitApi) = AuthApi(retrofitApi)
}