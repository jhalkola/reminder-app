package com.example.reminderapp.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.reminderapp.R
import com.example.reminderapp.ReminderWorker
import com.example.reminderapp.databinding.ActivityMainBinding
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get shared preferences
        sharedPref = applicationContext.getSharedPreferences(
                getString(R.string.sharedPref), Context.MODE_PRIVATE)

        checkLoginStatus()

        // set status bar color to black
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        // enable navigation through bottom navigation bar
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavView = binding.bottomNavigationView
        NavigationUI.setupWithNavController(bottomNavView, navController)

        // set toolbar to be used as support action bar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun checkLoginStatus() {
        val loginStatus = sharedPref.getInt(getString(R.string.login_key), 0)
        if (loginStatus == 0) {
            val loginIntent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    /**
     * unfocus EditText box when clicked outside of it
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()
                    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        checkLoginStatus()
    }

    companion object {
        fun showNotification(context: Context, message: String, notificationID: Int) {
            val channelID= "reminder_app_channel_id"
            val channelName = "Reminder App"

            val notificationBuilder = NotificationCompat.Builder(context, channelID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(channelName)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(channelID)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelID, channelName,
                    NotificationManager.IMPORTANCE_DEFAULT).apply {

                }
                manager.createNotificationChannel(channel)
            }
            manager.notify(notificationID, notificationBuilder.build())
        }

        fun scheduleNotification(context: Context, uid: Int, tag: String,
                                 timeInMillis: Long, message: String) {
            val workManager = WorkManager.getInstance(context)
            val reminderParameters = Data.Builder()
                .putString("message", message)
                .putInt("uid", uid)
                .build()

            // get minutes from now until reminder
            var minutesFromNow = 0L
            if (timeInMillis > System.currentTimeMillis())
                minutesFromNow = timeInMillis - System.currentTimeMillis()

            val notificationWork = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
                    .setInputData(reminderParameters)
                    .setInitialDelay(minutesFromNow, TimeUnit.MILLISECONDS)
                    .addTag(tag)
                    .build()
            workManager.enqueue(notificationWork)
        }

        fun cancelNotification(context: Context, tag: String) {
            val workManager = WorkManager.getInstance(context)
            // use tag to delete queued work
            workManager.cancelAllWorkByTag(tag)
        }
    }
}