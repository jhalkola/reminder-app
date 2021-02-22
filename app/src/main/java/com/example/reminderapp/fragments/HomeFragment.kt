package com.example.reminderapp.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reminderapp.R
import com.example.reminderapp.adaptors.ReminderAdapter
import com.example.reminderapp.databinding.FragmentHomeBinding
import com.example.reminderapp.db.viewmodels.ReminderViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var listView: ListView
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var adapter: ReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // set toolbar options
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(false)
        supActionBar.title = "Reminders"

        // ListView
        adapter = ReminderAdapter(requireContext())
        listView = binding.listViewMain
        listView.adapter = adapter

        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)
        mReminderViewModel.readAllReminders.observe(viewLifecycleOwner, Observer { reminder ->
            adapter.setData(reminder)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonAddReminder.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addReminderFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}