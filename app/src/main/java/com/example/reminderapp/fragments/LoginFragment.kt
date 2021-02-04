package com.example.reminderapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.reminderapp.activities.MainActivity
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        checkLoginStatus()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonLogin.setOnClickListener {
            checkCredentials()
            checkLoginStatus()
        }
        binding.textRegisterButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val loginStatus = (activity as Context).applicationContext.getSharedPreferences(getString(R.string.sharedPref), Context.MODE_PRIVATE).getInt(getString(R.string.login_key), 0)
        if (loginStatus == 1) {
            val loginIntent = Intent(activity as Context, MainActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun checkCredentials() {
        val username = binding.textUsername.text.toString()
        val password = binding.textPassword.text.toString()

        val sharedPref = (activity as Context).applicationContext.getSharedPreferences(getString(R.string.sharedPref), Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString(username.plus(R.string.user_key), null)
        val savedPassword = sharedPref.getString(username.plus(R.string.pass_key), null)

        if (savedUsername == username && savedPassword == password) {
            sharedPref.edit().putInt(getString(R.string.login_key), 1).apply()
            sharedPref.edit().putString(getString(R.string.current_user_key), username).apply()
        }
        else {
            val toast = Toast.makeText(activity as Context, "Login credentials were incorrect", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}