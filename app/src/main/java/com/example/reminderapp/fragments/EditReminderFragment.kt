package com.example.reminderapp.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentReminderBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.entities.User
import com.example.reminderapp.db.viewmodels.ReminderViewModel


class EditReminderFragment : Fragment() {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditReminderFragmentArgs>()
    private lateinit var mReminderViewModel: ReminderViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)

        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(true)
        supActionBar.title = ""

        // display reminder data, edit fragment is both showing the reminder and allows editing it
        binding.textHeader.setText(args.currentReminder.header)
        binding.textBody.setText(args.currentReminder.body)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            updateReminder()
            true
        }
        R.id.confirmReminder -> {
            updateReminder()
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

    private fun updateReminder() {
        val header = binding.textHeader.text.toString()
        val body = binding.textBody.text.toString()

        val reminder = Reminder(
                args.currentReminder.id,
                header,
                body,
                args.currentReminder.location_x,
                args.currentReminder.location_y,
                args.currentReminder.reminder_time,
                args.currentReminder.creation_time,
                args.currentReminder.creator_id,
                args.currentReminder.reminder_seen
        )
        mReminderViewModel.updateReminder(reminder)
        findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment)
    }
}