package com.example.reminderapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.reminderapp.activities.MainActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

class ReminderWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val message = inputData.getString("message")
        val notificationID = inputData.getInt("notification_id", 0)
        val lat = inputData.getDouble("lat", 0.0)
        val lng = inputData.getDouble("lng", 0.0)

        if (lat == 0.0 && lng == 0.0) {
            MainActivity.showNotification(applicationContext, message!!, notificationID)
        } else {
            validateLocation(lat, lng, message!!, notificationID)
        }

        return Result.success()
    }

    private fun validateLocation(lat: Double, lng: Double, message: String, notificationID: Int) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                        applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("worker", "location cannot be accessed")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            val reminderLocation = Location("")
            reminderLocation.latitude = lat
            reminderLocation.longitude = lng

            val distance = it.distanceTo(reminderLocation)
            Log.d("worker", "distance: $distance")
            if (distance < 150) {
                MainActivity.showNotification(applicationContext, message, notificationID)
            }
        }
    }
}