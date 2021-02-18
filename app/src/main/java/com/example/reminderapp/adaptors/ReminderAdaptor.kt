package com.example.reminderapp.adaptors

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.reminderapp.R

class ReminderAdaptor(context: Context): BaseAdapter() {
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val row = layoutInflater.inflate(R.layout.row_main, parent, false)


        return row
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): Any {
        return "test string"
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}