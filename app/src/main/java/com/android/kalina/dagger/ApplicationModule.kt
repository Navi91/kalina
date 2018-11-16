package com.android.kalina.dagger

import android.arch.persistence.room.Room
import android.content.Context
import com.android.kalina.api.radio.RadioStateController
import com.android.kalina.api.retrofit.RetrofitApi
import com.android.kalina.api.retrofit.RetrofitCreator
import com.android.kalina.api.util.Preferences
import com.android.kalina.app.Studio21Application
import com.android.kalina.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Inject
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Studio21Application) {

    @Provides
    fun provideContext(): Context = application

    @Provides
    fun provideApplication(): Studio21Application = application

    @Provides
    @Singleton
    fun providePreferences(context: Context) = Preferences(context)

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "studio21-db").fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRadioStateController() = RadioStateController()
}