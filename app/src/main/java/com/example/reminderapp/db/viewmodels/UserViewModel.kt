package com.example.reminderapp.db.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.db.AppDatabase
import com.example.reminderapp.db.entities.User
import com.example.reminderapp.db.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application): AndroidViewModel(application) {
    val readAllUsers: LiveData<List<User>>
    val readAllEmails: LiveData<List<String>>
    private val repository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        readAllUsers = repository.readAllUsers
        readAllEmails = repository.readAllEmails
    }

    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
        }
    }

    fun readUser(name: String): LiveData<User> {
        return repository.readUser(name)
    }
}