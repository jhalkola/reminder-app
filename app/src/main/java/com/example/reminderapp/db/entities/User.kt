package com.example.reminderapp.db.entities

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "user_table")
data class User(
        @PrimaryKey(autoGenerate = true)
        val id: Int,
        val username: String,
        val email: String,
        val profileImageUri: String
): Parcelable