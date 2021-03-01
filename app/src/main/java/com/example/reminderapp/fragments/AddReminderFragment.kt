package com.example.reminderapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reminderapp.R
import com.example.reminderapp.activities.MainActivity
import com.example.reminderapp.databinding.FragmentReminderBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.viewmodels.ReminderViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random


class AddReminderFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val pickImage = 1
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var imageUri: Uri
    private var reminderTime = ""
    private var locationX = 0.0
    private var locationY = 0.0
    private var day = 0
    private var month = 0
    private var year = 0
    private var minute = 0
    private var hour = 0
    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedMinute = 0
    private var savedHour = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)

        sharedPref = (activity as Context).applicationContext.getSharedPreferences(
            getString(R.string.sharedPref), Context.MODE_PRIVATE)

        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(true)
        supActionBar.title = ""

        // init imageUri to empty for database entry
        imageUri = Uri.parse("")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imageReminder.setOnLongClickListener {
            deleteImage()
            true
        }
        binding.textNotificationTime.setOnClickListener {
            changeNotification()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        menu.findItem(R.id.deleteReminder).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            saveReminder()
            findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment)
            true
        }
        R.id.confirmReminder -> {
            if (saveReminder()) { findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment) }
            true
        }
        R.id.setNotificationReminder -> {
            pickNotificationTime()
            true
        }
        R.id.addImage -> {
            pickImage()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun saveReminder(): Boolean {
        val message = binding.textMessage.text.toString()
        val image = imageUri.toString()
        val creationTime = LocalDateTime.now().toString()
        val creatorID = Random.nextInt(10, 1000) + 5
        // create unique tag for reminder
        val tag = "reminder$creatorID"
        val reminderSeen = false

        return if (message.isNotEmpty()) {
            if (reminderTime.isNotEmpty()) {
                val millis = getReminderInMillis()
                MainActivity.scheduleNotification(
                    activity as Context,
                    creatorID,
                    tag,
                    millis,
                    message
                )
            }
            val reminder = Reminder(0,
                    message,
                    image,
                    locationX,
                    locationY,
                    reminderTime,
                    creationTime,
                    creatorID,
                    tag,
                    reminderSeen
            )
            println(reminder.id)
            mReminderViewModel.addReminder(reminder)
            true
        } else {
            Toast.makeText(requireContext(), "Message field cannot be empty. Reminder not saved", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun getReminderInMillis(): Long {
        val formatter = DateTimeFormatter.ofPattern("d.M.yyyy H.m")
        val date = LocalDateTime.parse(reminderTime, formatter)
        // add timezone to the reminderTime
        val timeZoneDateTime = date.atZone(ZoneId.of("EET"))
        // return reminderTime in milliseconds
        return timeZoneDateTime.toInstant().toEpochMilli()
    }

    private fun deleteImage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_, _ ->
            val imageUri = Uri.parse("")
            binding.imageReminder.setImageURI(imageUri)
        }
        builder.setNegativeButton("No") {_, _ -> }
        builder.setTitle("Remove image?")
        builder.create().show()
    }

    /**
     * Pick image through default gallery of the phone
     */
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, pickImage)
    }

    /**
     * If acceptable result returned, save image Uri to global variable
     * for saving purposes and display image
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data!!
            Glide.with(this)
                    .load(imageUri)
                    .into(binding.imageReminder)
        }
    }

    /**
     * Alert dialog with change and delete options
     */
    private fun changeNotification() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Edit") {_, _ ->
            pickNotificationTime()
        }
        builder.setNegativeButton("Delete") {_, _ ->
            reminderTime = ""
            binding.textNotificationTime.text = ""
            binding.textNotificationTime.toggleVisibility()
        }
        builder.setNeutralButton("Cancel") {_, _ -> }
        builder.setTitle("Change or delete notification of the reminder")
        builder.create().show()
    }

    private fun View.toggleVisibility() {
        isVisible = !isVisible
    }

    private fun formatDate(date: String): String {
        val formatter = DateTimeFormatter.ofPattern("d.M.yyyy H.m")
        val dt = LocalDateTime.parse(date, formatter)
        val year = dt.year
        val month = dt.month.toString().toLowerCase(Locale.ROOT).capitalize(Locale.ROOT)
        val day = dt.dayOfMonth
        val hour = dt.hour
        val minute = if (dt.minute < 10) {
            "0${dt.minute}"
        } else {
            dt.minute
        }
        return "$day. $month $year at $hour.$minute"
    }

    private fun pickNotificationTime() {
        getDateTimeCalendar()
        DatePickerDialog(requireContext(), this, year, month, day).show()
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+2:00"))
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        minute = cal.get(Calendar.MINUTE)
        hour = cal.get(Calendar.HOUR_OF_DAY)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        // return month as int from 0-11. Adding 1 to month as should be from 1-12
        savedMonth = month + 1
        savedYear = year

        getDateTimeCalendar()
        TimePickerDialog(requireContext(), this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedMinute = minute
        savedHour = hourOfDay

        reminderTime = "$savedDay.$savedMonth.$savedYear $savedHour.$savedMinute"
        binding.textNotificationTime.isVisible = true
        binding.textNotificationTime.text = formatDate(reminderTime)
    }
}
