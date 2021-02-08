package com.example.reminderapp.db.repositories

import androidx.lifecycle.LiveData
import com.example.reminderapp.db.daos.ReminderDao
import com.example.reminderapp.db.entities.Reminder

class ReminderRepository(private val reminderDao: ReminderDao) {
    val readAllReminders: LiveData<List<Reminder>> = reminderDao.readAllReminders()

    suspend fun addReminder(reminder: Reminder) {
        reminderDao.addReminder(reminder)
    }
}