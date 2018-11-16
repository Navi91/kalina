package com.android.kalina.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.android.kalina.database.message.Message
import com.android.kalina.database.message.MessageDao

@Database(entities = [(Message::class)], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun messageDao(): MessageDao
}