package com.example.reminderapp.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reminderapp.activities.MainActivity
import com.example.reminderapp.R
import com.example.reminderapp.activities.LoginActivity
import com.example.reminderapp.databinding.FragmentLoginBinding
import com.example.reminderapp.db.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // get shared preferences
        sharedPref = (activity as Context).applicationContext.getSharedPreferences(
                getString(R.string.sharedPref), Context.MODE_PRIVATE)

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        textInputUsername = binding.textFieldUsernameLogin
        textInputPassword = binding.textFieldPasswordLogin

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonLogin.setOnClickListener {
            checkCredentials()
        }
        binding.textRegisterButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun checkCredentials() {
        val username = textInputUsername.editText?.text.toString()
        val password = textInputPassword.editText?.text.toString()
        val savedUsername = sharedPref.getString(username.plus(R.string.user_key), null)
        val savedPassword = sharedPref.getString(username.plus(R.string.pass_key), null)

        if (savedUsername == username && savedPassword == password) {
            var userId = 0
            mUserViewModel.readUserId(username).observe(viewLifecycleOwner, { id ->
                userId = id
            })

            sharedPref.edit().apply{
                putInt(getString(R.string.login_key), 1)
                putInt(getString(R.string.current_user_id_key), userId)
                putString(getString(R.string.current_user_key), username)
            }.apply()

            val mainIntent = Intent(activity as Context, MainActivity::class.java)
            startActivity(mainIntent)
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