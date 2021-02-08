package com.example.reminderapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.reminderapp.db.daos.ReminderDao
import com.example.reminderapp.db.daos.UserDao
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.entities.User

@Database(entities = [User::class, Reminder::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: com.example.reminderapp.db.AppDatabase? = null

        fun getDatabase(context: Context): com.example.reminderapp.db.AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
