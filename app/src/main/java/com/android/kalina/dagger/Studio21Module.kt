package com.android.kalina.dagger

import com.android.kalina.api.radio.RadioStateController
import com.android.kalina.dagger.api.AuthModule
import com.android.kalina.dagger.api.ChatModule
import com.android.kalina.dagger.api.RetrofitModule
import dagger.Module
import javax.inject.Inject
import javax.inject.Singleton

@Module(includes = [AuthModule::class, RetrofitModule::class, ChatModule::class])
class Studio21Module {


}