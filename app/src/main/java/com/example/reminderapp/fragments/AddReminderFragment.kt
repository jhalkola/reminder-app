package com.example.reminderapp.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentReminderBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.viewmodels.ReminderViewModel
import java.time.LocalDateTime
import java.time.Month


class AddReminderFragment : Fragment() {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mReminderViewModel: ReminderViewModel

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

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            saveReminder()
            findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment)
            true
        }
        R.id.confirmReminder -> {
            saveReminder()
            findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment)
            true
        }
        R.id.setNotificationReminder -> {
            TODO()
        }
        R.id.deleteReminder -> {
            TODO()
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun saveReminder() {
        val header = binding.textHeader.text.toString()
        val body = binding.textBody.text.toString()
        val locationX = 0.0
        val locationY = 0.0
        val reminderTime = LocalDateTime.of(2021, Month.FEBRUARY, 20, 14, 0).toString()
        val creationTime = LocalDateTime.now().toString()
        val creatorID = sharedPref.getInt(getString(R.string.current_user_id_key), 0)
        val reminderSeen = false

        if (inputCheck(header, body)) {
            val reminder = Reminder(0, header, body, locationX, locationY, reminderTime, creationTime, creatorID, reminderSeen)
            mReminderViewModel.addReminder(reminder)
        }
    }

    private fun inputCheck(header: String, body: String): Boolean {
        return !(TextUtils.isEmpty(header) || TextUtils.isEmpty(body))
    }
}
