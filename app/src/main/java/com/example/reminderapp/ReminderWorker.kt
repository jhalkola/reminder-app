package com.example.reminderapp

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.reminderapp.activities.MainActivity

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val message = inputData.getString("message")
        val notificationID = inputData.getInt("uid", 0)
        MainActivity.showNotification(applicationContext, message!!, notificationID)
        return Result.success()
    }
}