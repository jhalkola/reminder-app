package com.example.reminderapp.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user_table ORDER BY id ASC")
    fun readAllUsers(): LiveData<List<User>>

    @Query("SELECT email FROM user_table")
    fun readEmails(): LiveData<List<String>>

    @Query("SELECT * FROM user_table WHERE username = :name")
    fun readUser(name: String): LiveData<User>

    @Query("SELECT id FROM user_table WHERE username = :name")
    fun readUserId(name: String): LiveData<Int>
}