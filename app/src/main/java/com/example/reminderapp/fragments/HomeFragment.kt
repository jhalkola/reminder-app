package com.example.reminderapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.example.reminderapp.R
import com.example.reminderapp.activities.MainActivity
import com.example.reminderapp.adapters.ReminderAdapter
import com.example.reminderapp.databinding.FragmentHomeBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.viewmodels.ReminderViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var listView: ListView
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var adapter: ReminderAdapter
    private var reminderList = mutableListOf<Reminder>()
    private var hiddenReminders = MutableLiveData(true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // set toolbar options
        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(false)
        supActionBar.title = "Reminders"

        // ListView
        adapter = ReminderAdapter()
        listView = binding.listViewMain
        listView.adapter = adapter
        adapter.notifyDataSetChanged()

        val creatorID = arguments?.getInt("id")
        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)
        mReminderViewModel.readAllReminders.observe(viewLifecycleOwner, { reminders ->
            if (creatorID != null) {
                for (reminder in reminders) {
                    if (reminder.creator_id == creatorID) {
                        val action = HomeFragmentDirections.actionHomeFragmentToEditReminderFragment(reminder)
                        findNavController().navigate(action)
                    }
                }
            }

            for (reminder in reminders) {
                val workInfo = MainActivity.workStatus(activity as Context, reminder.request_tag)
                if (reminder.reminder_seen or
                        (!reminder.reminder_seen && reminder.reminder_time.isEmpty())) {
                    reminderList.add(reminder)
                } else if (!reminder.reminder_seen && workInfo.get().size == 1) {
                    if (workInfo.get()[0].state == WorkInfo.State.SUCCEEDED) {
                        reminder.reminder_seen = true
                        mReminderViewModel.updateReminder(reminder)
                        reminderList.add(reminder)
                    }
                }
            }
            hiddenReminders.observe(viewLifecycleOwner, { hidden ->
                if (hidden) {
                    adapter.setData(reminderList as List<Reminder>)
                } else {
                    adapter.setData(reminders)
                }
            })
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addReminderFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.showAllReminder) {
            hiddenReminders.value
            if (hiddenReminders.value!!) {
                hiddenReminders.value = false
                item.icon = ContextCompat.getDrawable(activity as Context, R.drawable.ic_visible)
            } else {
                hiddenReminders.value = true
                item.icon = ContextCompat.getDrawable(activity as Context, R.drawable.ic_invisible)
            }
            true
        } else { return super.onOptionsItemSelected(item) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}