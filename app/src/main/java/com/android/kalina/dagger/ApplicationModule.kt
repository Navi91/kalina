package com.android.kalina.dagger

import android.arch.persistence.room.Room
import android.content.Context
import com.android.kalina.api.radio.RadioStateController
import com.android.kalina.api.util.Preferences
import com.android.kalina.app.KalinaApplication
import com.android.kalina.database.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: KalinaApplication) {

    @Provides
    fun provideContext(): Context = application

    @Provides
    fun provideApplication(): KalinaApplication = application

    @Provides
    @Singleton
    fun providePreferences(context: Context) = Preferences(context)

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "kalina-db").fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideRadioStateController() = RadioStateController()
}