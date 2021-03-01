package com.example.reminderapp.db.entities

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "username") val username: String,
        @ColumnInfo(name = "email") val email: String,
        @ColumnInfo(name = "image_uri") val imageUri: String
): Parcelable