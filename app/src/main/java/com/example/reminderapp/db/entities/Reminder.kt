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
        @ColumnInfo(name = "message") var message: String,
        @ColumnInfo(name = "image_uri") var imageUri: String,
        @ColumnInfo(name = "location_x") var location_x: Double?,
        @ColumnInfo(name = "location_y") var location_y: Double?,
        @ColumnInfo(name = "reminder_time") var reminder_time: String,
        @ColumnInfo(name = "creation_time") var creation_time: String,
        @ColumnInfo(name = "creator_id") var creator_id: Int,
        @ColumnInfo(name = "request_tag") var request_tag: String,
        @ColumnInfo(name = "reminder_seen") var reminder_seen: Boolean
): Parcelable