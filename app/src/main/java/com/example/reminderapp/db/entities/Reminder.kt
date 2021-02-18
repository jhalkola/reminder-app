package com.example.reminderapp.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_table")
data class Reminder(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val header: String,
        val body: String,
        val location_x: Double,
        val location_y: Double,
        val reminder_time: String,
        val creation_time: String,
        val creator_id: Int,
        val reminder_seen: Boolean
)