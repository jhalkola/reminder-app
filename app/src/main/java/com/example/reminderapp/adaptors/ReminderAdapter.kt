package com.example.reminderapp.adaptors

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.reminderapp.R
import com.example.reminderapp.databinding.RowMainBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.viewmodels.ReminderViewModel
import com.example.reminderapp.fragments.HomeFragmentDirections
import java.time.LocalDateTime
import java.util.*

class ReminderAdapter(context: Context): BaseAdapter() {
    private var reminderList = emptyList<Reminder>()
    private lateinit var selectedReminder: Reminder

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row: RowMainBinding
        var result = convertView
        val currentReminder = reminderList[position]
        if (convertView == null) {
            row = RowMainBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            result = row.root
            result.tag = row
            row.textMessage.text = currentReminder.message
            row.textReminderCreated.text = formatDate(currentReminder.creation_time)

        } else {
            row = result?.tag as RowMainBinding
        }
        row.rowLayout.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToEditReminderFragment(currentReminder)
            parent?.findNavController()?.navigate(action)
        }
        row.rowLayout.setOnLongClickListener {
            selectedReminder = currentReminder
            true
        }
        return row.root
    }

    override fun getCount(): Int {
        return reminderList.size
    }

    override fun getItem(position: Int): Any {
        return reminderList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getReminders(): Reminder {
        return selectedReminder
    }

    fun setData(reminder: List<Reminder>) {
        this.reminderList = reminder
        notifyDataSetChanged()
    }

    private fun formatDate(date: String): String {
        val dt = LocalDateTime.parse(date)
        val year = dt.year
        val month = dt.month.toString().toLowerCase(Locale.ROOT)
        val day = dt.dayOfMonth
        val hour = dt.hour
        val minute = dt.minute
        return "$day.$month $year at $hour.$minute"
    }
}