package com.example.reminderapp.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "reminder_table")
data class Reminder(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "message") val message: String,
        @ColumnInfo(name = "image_uri") val imageUri: String,
        @ColumnInfo(name = "location_x") val location_x: Double,
        @ColumnInfo(name = "location_y") val location_y: Double,
        @ColumnInfo(name = "reminder_time") val reminder_time: String,
        @ColumnInfo(name = "creation_time") val creation_time: String,
        @ColumnInfo(name = "creator_id") val creator_id: Int,
        @ColumnInfo(name = "request_tag") val request_tag: String,
        @ColumnInfo(name = "reminder_seen") val reminder_seen: Boolean
): Parcelable