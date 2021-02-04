package com.example.reminderapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.reminderapp.R
import com.example.reminderapp.activities.LoginActivity
import com.example.reminderapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.logoutButton.setOnClickListener {
            setLoginStatus()
            val logoutIntent = Intent(activity as Context, LoginActivity::class.java)
            startActivity(logoutIntent)
        }
    }

    private fun setLoginStatus() {
        (activity as Context).applicationContext.getSharedPreferences(getString(R.string.sharedPref), Context.MODE_PRIVATE).edit().putInt("loginKey", 0).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}