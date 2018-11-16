package com.android.kalina.database.message

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface MessageDao {

    @Query("SELECT * FROM message ORDER BY created_at")
    fun getAll(): LiveData<List<Message>>

    @Query("SELECT * FROM message WHERE id LIKE -1")
    fun getAllUnsent(): List<Message>

    @Insert
    fun insertAll(vararg messages: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<Message>)

    @Query("DELETE FROM message WHERE client_id = :clientId")
    fun deleteByClientId(clientId: String)

    @Query("UPDATE message SET id = :id WHERE client_id = :clientId")
    fun updateMessageIdWithClientId(id: Long, clientId: String)
}