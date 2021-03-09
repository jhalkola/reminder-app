package com.example.reminderapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import org.w3c.dom.Text
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class EditReminderFragment : Fragment(), DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, TextToSpeech.OnInitListener {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditReminderFragmentArgs>()
    private val pickImage = 1
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var reminder: Reminder
    private lateinit var oldReminderTime: String
    private lateinit var oldMessage: String
    private var tts: TextToSpeech? = null
    private val finnish = Locale("fi", "FI")
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

        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(true)
        supActionBar.title = ""

        reminder = args.currentReminder
        loadReminderData()

        tts = TextToSpeech(requireContext(), this)
        oldReminderTime = args.currentReminder.reminder_time
        oldMessage = args.currentReminder.message

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
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

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            updateReminder()
            findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment)
            true
        }
        R.id.confirmReminder -> {
            if (updateReminder()) { findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment) }
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
        R.id.deleteReminder -> {
            deleteReminder()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun loadReminderData() {
        val uri = Uri.parse(reminder.imageUri)
        val reminderTime = reminder.reminder_time
        Glide.with(this)
            .load(uri)
            .into(binding.imageReminder)
        binding.textMessage.setText(reminder.message)
        if (reminderTime.isNotEmpty()) {
            binding.textNotificationTime.toggleVisibility()
            binding.textNotificationTime.text = formatDate(reminderTime)
        }
    }

    private fun deleteImage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_, _ ->
            val uri = Uri.parse("")
            reminder.imageUri = ""
            binding.imageReminder.setImageURI(uri)
        }
        builder.setNegativeButton("No") {_, _ -> }
        builder.setTitle("Remove image?")
        builder.create().show()
    }

    private fun deleteReminder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mReminderViewModel.deleteReminder(args.currentReminder)
            MainActivity.cancelNotification(activity as Context, args.currentReminder.request_tag)
            Toast.makeText(requireContext(), "Reminder removed", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete reminder?")
        builder.setMessage("Are you sure you want to delete this reminder?")
        builder.create().show()
    }

    private fun updateReminder(): Boolean {
        val message = binding.textMessage.text.toString()
        val tag = reminder.request_tag
        val reminderSeen = reminder.reminder_seen
        val reminderTime = reminder.reminder_time

        return if (message.isNotEmpty()) {
            reminder.message = message
            mReminderViewModel.updateReminder(reminder)

            if (reminderTime.isEmpty() and (oldReminderTime != reminderTime)) {
                MainActivity.cancelNotification(activity as Context, tag)
            } else if (!reminderSeen and reminderTime.isNotEmpty()) {
                if ((oldReminderTime != reminderTime) or (oldMessage != message)) {
                    val millis = getReminderInMillis()
                    MainActivity.cancelNotification(activity as Context, tag)
                    MainActivity.scheduleNotification(activity as Context, reminder, millis)
                }
            }
            true
        } else {
            Toast.makeText(activity as Context, "Message field cannot be empty. Reminder was not changed", Toast.LENGTH_SHORT).show()
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

    private fun chooseTimeOrLocation() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Location") {_, _ ->
            val action = EditReminderFragmentDirections.actionEditReminderFragmentToMapsFragment(reminder, "editFragment")
            findNavController().navigate(action)
        }
        builder.setNegativeButton("Time") {_, _ ->
            pickNotificationTime()
        }
        builder.setNeutralButton("Cancel") {_, _ -> }
        builder.setTitle("Choose location or time")
        builder.setMessage("Do you want to choose reminder location or time and date?")
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

    private fun changeNotification() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Edit") {_, _ ->
            pickNotificationTime()
        }
        builder.setNegativeButton("Delete") {_, _ ->
            reminder.reminder_time = ""
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
        if (date.isNotEmpty()) {
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
        } else {
            return ""
        }
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