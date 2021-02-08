package com.example.reminderapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val header: String,
    val body: String
)