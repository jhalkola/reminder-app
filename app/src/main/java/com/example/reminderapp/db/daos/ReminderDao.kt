package com.example.reminderapp.db.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.reminderapp.db.entities.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addReminder(reminder: Reminder)

    @Query("SELECT * FROM reminder_table ORDER BY id ASC")
    fun readAllReminders(): LiveData<List<Reminder>>
}