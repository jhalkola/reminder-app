package com.example.reminderapp

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import android.widget.ListView
import com.example.reminderapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        listView = binding.listViewMain
        val list = arrayListOf("item 1", "item 2", "item 3", "item 4", "item 5", "item 6")
        val adapter = ArrayAdapter(activity as Context, android.R.layout.simple_list_item_1, list)
        listView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}