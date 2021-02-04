package com.example.reminderapp.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentRegisterBinding
import com.google.android.material.textfield.TextInputLayout


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val pickImage = 1
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.imageProfilePictureRegister.clipToOutline = true

        sharedPref = (activity as Context).applicationContext.getSharedPreferences(
                getString(R.string.sharedPref), Context.MODE_PRIVATE)

        textInputUsername = binding.textFieldUsernameRegister
        textInputPassword = binding.textFieldPasswordRegister
        textInputEmail = binding.textFieldEmailRegister
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonRegister.setOnClickListener {
            val registrationSuccessful = saveData()
            if (registrationSuccessful) {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
        binding.buttonAddImage.setOnClickListener {
            pickImage()
        }
    }

    private fun saveData(): Boolean {
        val username = textInputUsername.editText?.text.toString()
        val password = textInputPassword.editText?.text.toString()
        val email = textInputEmail.editText?.text.toString()

        if (!validateFields()) {
            return false
        } else {
            val editor = sharedPref.edit()
            editor.apply {
                putString(username.plus(R.string.user_key), username)
                putString(username.plus(R.string.pass_key), password)
                putString(username.plus(R.string.email_key), email)
                putString(email.plus(R.string.email_key), email)
            }.apply()

            textInputUsername.error = ""
            textInputPassword.error = ""
            textInputEmail.error = ""
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK) {
            val imageURI = data?.data
            binding.imageProfilePictureRegister.setImageURI(imageURI)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, pickImage)
    }

    private fun validateFields(): Boolean {
        return !(!validateUsername() or !validatePassword() or !validateEmail())
    }

    private fun validateUsername(): Boolean {
        val username = textInputUsername.editText?.text.toString()
        return when {
            username.isEmpty() -> {
                textInputUsername.error = "Field can't be empty"
                false
            }
            username.length > 12 -> {
                textInputUsername.error = "Username has to be under 12 characters"
                false
            }
            sharedPref.contains(username.plus(R.string.user_key)) -> {
                textInputUsername.error = "Username already in use"
                false
            }
            else -> {
                textInputUsername.error = ""
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = textInputPassword.editText?.text.toString()
        return when {
            password.isEmpty() -> {
                textInputPassword.error = "Field can't be empty"
                false
            }
            password.length > 20 -> {
                textInputPassword.error = "Password has to be under 20 characters"
                false
            }
            else -> {
                textInputPassword.error = ""
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = textInputEmail.editText?.text.toString()
        return when {
            email.isEmpty() -> {
                textInputEmail.error = "Field can't be empty"
                false
            }
            sharedPref.contains(email.plus(R.string.email_key)) -> {
                textInputEmail.error = "Email already registered to another account"
                false
            }
            else -> {
                textInputEmail.error = ""
                true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}