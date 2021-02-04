package com.example.reminderapp.db

import androidx.lifecycle.LiveData

class ReminderRepository(private val reminderDao: ReminderDao) {
    val readAllData: LiveData<List<Reminder>> = reminderDao.readAllData()

    suspend fun addReminder(reminder: Reminder) {
        reminderDao.addReminder(reminder)
    }
}