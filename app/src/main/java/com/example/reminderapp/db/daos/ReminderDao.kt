package com.example.reminderapp.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.entities.User

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addReminder(reminder: Reminder)

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("SELECT * FROM reminder_table ORDER BY id ASC")
    fun readAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM reminder_table WHERE creator_id = :id")
    fun readReminderByCreatorId(id: Int): LiveData<Reminder>
}