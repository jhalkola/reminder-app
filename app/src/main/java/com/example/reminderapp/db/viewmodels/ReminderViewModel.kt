package com.example.reminderapp.db.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.db.AppDatabase
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.entities.User
import com.example.reminderapp.db.repositories.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application): AndroidViewModel(application) {
    val readAllReminders: LiveData<List<Reminder>>
    private val repository: ReminderRepository

    init {
        val reminderDao = AppDatabase.getDatabase(application).reminderDao()
        repository = ReminderRepository(reminderDao)
        readAllReminders = repository.readAllReminders
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addReminder(reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(reminder)
        }
    }
}