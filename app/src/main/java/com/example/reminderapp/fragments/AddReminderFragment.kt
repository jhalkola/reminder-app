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
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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


class AddReminderFragment : Fragment(), DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, TextToSpeech.OnInitListener {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<AddReminderFragmentArgs>()
    private val pickImage = 1
    private val finnish = Locale("fi", "FI")
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var reminder: Reminder
    private var tts: TextToSpeech? = null
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

        reminder = args.currentReminder
        loadReminderData()

        tts = TextToSpeech(requireContext(), this)

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
        binding.imageTts.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "Reminder does not have message",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                speakOut(message)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        menu.findItem(R.id.deleteReminder).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            saveReminder()
            findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment)
            true
        }
        R.id.confirmReminder -> {
            if (saveReminder()) {
                findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment)
            }
            true
        }
        R.id.setNotificationReminder -> {
            chooseTimeOrLocation()
            true
        }
        R.id.addImage -> {
            pickImage()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun loadReminderData() {
        val uri = Uri.parse(reminder.imageUri)
        val reminderTime = reminder.reminder_time
        if (uri.toString().isNotEmpty()) {
            Glide.with(this)
                .load(uri)
                .into(binding.imageReminder)
        }
        binding.textMessage.setText(reminder.message)
        if (reminderTime.isNotEmpty()) {
            binding.textNotificationTime.toggleVisibility()
            binding.textNotificationTime.text = formatDate(reminderTime)
        }
    }

    private fun getCurrentReminder() {
        val message = binding.textMessage.text.toString()
        val creatorID = Random.nextInt(10, 1000) + 5
        // create unique tag for reminder
        val tag = "reminder$creatorID"

        reminder.message = message
        reminder.creator_id = creatorID
        reminder.request_tag = tag
    }

    private fun saveReminder(): Boolean {
        getCurrentReminder()
        val message = reminder.message

        return if (message.isNotEmpty()) {
            // update creation time to match when save is done
            reminder.creation_time = LocalDateTime.now().toString()
            mReminderViewModel.addReminder(reminder)
            // schedule notification
            if (reminder.reminder_time.isNotEmpty()) {
                val millis = getReminderInMillis()
                MainActivity.scheduleNotification(
                        activity as Context,
                        reminder,
                        millis,
                )
            }
            Toast.makeText(activity,
                    "Reminder has been saved",
                    Toast.LENGTH_SHORT
            ).show()
            true
        } else {
            Toast.makeText(requireContext(), "Message field cannot be empty. Reminder not saved", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun getReminderInMillis(): Long {
        val formatter = DateTimeFormatter.ofPattern("d.M.yyyy H.m")
        val date = LocalDateTime.parse(reminder.reminder_time, formatter)
        // add timezone to the reminderTime
        val timeZoneDateTime = date.atZone(ZoneId.of("EET"))
        // return reminderTime in milliseconds
        return timeZoneDateTime.toInstant().toEpochMilli()
    }

    private fun deleteImage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            val uri = Uri.parse("")
            reminder.imageUri = ""
            binding.imageReminder.setImageURI(uri)
        }
        builder.setNegativeButton("No") { _, _ -> }
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
            val uri = data.data!!
            reminder.imageUri = uri.toString()
            Glide.with(this)
                    .load(uri)
                    .into(binding.imageReminder)
        }
    }

    /**
     * Alert dialog with change and delete options
     */
    private fun changeNotification() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Edit") { _, _ ->
            pickNotificationTime()
        }
        builder.setNegativeButton("Delete") { _, _ ->
            val reminderTime = ""
            reminder.reminder_time = reminderTime
            binding.textNotificationTime.text = ""
            binding.textNotificationTime.toggleVisibility()
        }
        builder.setNeutralButton("Cancel") { _, _ -> }
        builder.setTitle("Change or delete notification of the reminder")
        builder.create().show()
    }

    private fun chooseTimeOrLocation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Location") { _, _ ->
            getCurrentReminder()
            val action = AddReminderFragmentDirections.actionAddReminderFragmentToMapsFragment(reminder, "addFragment")
            findNavController().navigate(action)
        }
        builder.setNegativeButton("Time") { _, _ ->
            pickNotificationTime()
        }
        builder.setNeutralButton("Cancel") { _, _ -> }
        builder.setTitle("Choose location or time")
        builder.setMessage("Do you want to choose reminder location or time and date?")
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

        val reminderTime = "$savedDay.$savedMonth.$savedYear $savedHour.$savedMinute"
        reminder.reminder_time = reminderTime
        binding.textNotificationTime.isVisible = true
        binding.textNotificationTime.text = formatDate(reminderTime)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(finnish)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), "Language not supported by text to speech",
                        Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(requireContext(), "Error occurred",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun speakOut(message: String) {
        tts!!.speak(message, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}
