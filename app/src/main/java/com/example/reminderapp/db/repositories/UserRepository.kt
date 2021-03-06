package com.example.reminderapp.db.repositories

import androidx.lifecycle.LiveData
import com.example.reminderapp.db.daos.UserDao
import com.example.reminderapp.db.entities.User

class UserRepository(private val userDao: UserDao) {
    val readAllUsers: LiveData<List<User>> = userDao.readAllUsers()
    val readEmails: LiveData<List<String>> = userDao.readEmails()

    suspend fun addUser(user: User) {
        userDao.addUser(user)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    fun readUser(name: String): LiveData<User> {
        return userDao.readUser(name)
    }

    fun readUserId(name: String): LiveData<Int> {
        return userDao.readUserId(name)
    }
}