package com.example.reminderapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentRegisterBinding
import com.example.reminderapp.db.entities.User
import com.example.reminderapp.db.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputLayout


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val pickImage = 1
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var imageUri: Uri
    private lateinit var emailList: List<String>

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

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // get all emails from db. Async task did not have enough time to complete in emailValidation
        mUserViewModel.readEmails.observe(viewLifecycleOwner, { emails ->
            emailList = emails
        })

        // set default image to empty in case no profile picture is chosen
        imageUri = Uri.parse("")

        sharedPref.edit().putInt(getString(R.string.login_key), 0).apply()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonRegister.setOnClickListener {
            if (saveUserInformation()) {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
        binding.buttonAddImage.setOnClickListener {
            pickImage()
        }
    }

    /**
     * Save login info to shared preferences and user info to the database
     * Information saved: id (PK), username, email, profile picture Uri as string
     */
    private fun saveUserInformation(): Boolean {
        val username = textInputUsername.editText?.text.toString()
        val password = textInputPassword.editText?.text.toString()
        val email = textInputEmail.editText?.text.toString()

        return if (!validateFields()) {
            false
        } else {
            // add login info to shared preferences
            val editor = sharedPref.edit()
            editor.apply {
                putString(username.plus(R.string.user_key), username)
                putString(username.plus(R.string.pass_key), password)
            }.apply()

            // add user to the database
            val user = User(0, username, email, imageUri.toString())
            mUserViewModel.addUser(user)

            textInputUsername.error = ""
            textInputPassword.error = ""
            textInputEmail.error = ""
            true
        }
    }

    /**
     * Pick image through default gallery of the phone
     */
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, pickImage)
    }

    /**
     * If acceptable result returned, save image Uri to global variable
     * for saving purposes and display image
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data!!
            Glide.with(this)
                    .load(imageUri)
                    .into(binding.imageProfilePictureRegister)
        }
    }

    /**
     * Check correctness of fields: not empty, max length not exceeded and username or email
     * is not already in use
     */
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
            emailList.contains(email) -> {
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