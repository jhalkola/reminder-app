package com.example.reminderapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
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
        binding.imageProfilePictureProfile.clipToOutline = true
        loadUserInfo()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val username = "marmot"

        binding.buttonLogout.setOnClickListener {
            setLoginStatus()
            val logoutIntent = Intent(activity as Context, LoginActivity::class.java)
            startActivity(logoutIntent)
        }
    }

    private fun loadUserInfo() {
        val sharedPref = (activity as Context).applicationContext.getSharedPreferences(getString(R.string.sharedPref), Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString(getString(R.string.current_user_key), null)
        val savedUsername = sharedPref.getString(currentUser.plus(R.string.user_key), null)
        val savedPassword = sharedPref.getString(currentUser.plus(R.string.pass_key), null)
        val savedEmail = sharedPref.getString(currentUser.plus(R.string.email_key), null)

        val textUsername = binding.textUsernameProfile as EditText
        textUsername.setText(savedUsername)
        val textPassword = binding.textPasswordProfile as EditText
        textPassword.setText(savedPassword)
        val textEmail = binding.textEmailProfile as EditText
        textEmail.setText(savedEmail)
    }

    /*private fun changeUserInfo(username: String) {
        val sharedPref = (activity as Context).applicationContext.getSharedPreferences(getString(R.string.sharedPref), Context.MODE_PRIVATE)
        val currentUser = sharedPref.getString(getString(R.string.current_user_key), null)

        val textUsername = binding.textUsernameProfile.text
        if (username.isNotEmpty()) {
            when {
                sharedPref.contains(username.plus(R.string.user_key)) -> {
                    val toast = Toast.makeText(activity as Context, "Username already taken", Toast.LENGTH_SHORT)
                    toast.show()
                }
                sharedPref.contains(email.plus(R.string.email_key)) -> {
                    val toast = Toast.makeText(activity as Context, "Email already in use on another account", Toast.LENGTH_SHORT)
                    toast.show()
                }
                else -> {
                    val editor = sharedPref.edit()
                    editor.apply {
                        putString(username.plus(R.string.user_key), username)
                        putString(username.plus(R.string.pass_key), password)
                        putString(username.plus(R.string.email_key), email)
                        putString(email.plus(R.string.email_key), email)

                    }.apply()
                }
            }
        }
        else {
            val toast = Toast.makeText(activity as Context, "Cannot register with empty fields!", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }

    }*/

    private fun setLoginStatus() {
        (activity as Context).applicationContext.getSharedPreferences(getString(R.string.sharedPref), Context.MODE_PRIVATE).edit().putInt(getString(R.string.login_key), 0).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}