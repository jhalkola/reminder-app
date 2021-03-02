package com.example.reminderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.example.reminderapp.databinding.RowMainBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.fragments.HomeFragmentDirections
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ReminderAdapter : BaseAdapter() {
    private var reminderList = emptyList<Reminder>()
    private var spotList = mutableListOf<Pair<Int, Reminder>>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row: RowMainBinding
        var result = convertView
        val currentReminder = reminderList[position]

        if (convertView == null || Pair(position, currentReminder) !in spotList) {
            row = RowMainBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
            result = row.root
            result.tag = row
            if (currentReminder.reminder_time.isNotEmpty()) {
                row.imageNotiIcon.isVisible = true
                row.textReminderTime.text = formatReminderTime(currentReminder.reminder_time)
            }
            row.textMessage.text = currentReminder.message
            row.textReminderCreated.text = formatCreationTime(currentReminder.creation_time)
            spotList.add(Pair(position, currentReminder))
        } else {
            row = result?.tag as RowMainBinding
        }
        row.rowLayout.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToEditReminderFragment(currentReminder)
            parent?.findNavController()?.navigate(action)
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

    fun setData(reminders: List<Reminder>) {
        reminderList = reminders
        spotList = mutableListOf()
        notifyDataSetChanged()
    }

    private fun formatReminderTime(date: String): String {
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

    private fun formatCreationTime(date: String): String {
        val dt = LocalDateTime.parse(date)
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
}