package com.example.reminderapp.db

import android.media.Image
import android.provider.ContactsContract
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val username: String,
    val email: String
)