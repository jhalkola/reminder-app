package com.example.reminderapp.db.repositories

import androidx.lifecycle.LiveData
import com.example.reminderapp.db.daos.ReminderDao
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.entities.User

class ReminderRepository(private val reminderDao: ReminderDao) {
    val readAllReminders: LiveData<List<Reminder>> = reminderDao.readAllReminders()

    suspend fun addReminder(reminder: Reminder) {
        reminderDao.addReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }
}