package com.example.reminderapp.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentProfileEditBinding
import com.example.reminderapp.db.entities.User
import com.example.reminderapp.db.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputLayout

class ProfileEditFragment : Fragment() {
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    private val pickImage = 1
    private val args by navArgs<ProfileEditFragmentArgs>()
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var imageProfilePicture: ImageView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var currentUser: String
    private lateinit var mUserViewModel: UserViewModel
    private lateinit var imageUri: Uri

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        binding.imageProfilePictureProfile.clipToOutline = true

        sharedPref = (activity as Context).applicationContext.getSharedPreferences(
                getString(R.string.sharedPref), Context.MODE_PRIVATE)
        currentUser = sharedPref.getString(getString(R.string.current_user_key), null).toString()

        textInputUsername = binding.textFieldUsernameProfile
        textInputPassword = binding.textFieldPasswordProfile
        textInputEmail = binding.textFieldEmailProfile
        imageProfilePicture = binding.imageProfilePictureProfile

        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        loadUserInfo()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonAddImage.setOnClickListener {
            pickImage()
        }
        binding.buttonSaveChanges.setOnClickListener {
            if (updateUserInformation()) {
                findNavController().navigate(R.id.action_profileEditFragment_to_profileFragment)
            }
        }
    }

    /**
     * Load user's info to the profile page
     */
    private fun loadUserInfo() {
        val savedPassword = sharedPref.getString(currentUser.plus(R.string.pass_key), null)
        textInputPassword.editText?.setText(savedPassword)
        textInputUsername.editText?.setText(args.currentUserInfo.username)
        textInputEmail.editText?.setText(args.currentUserInfo.email)
        val imageUriString = args.currentUserInfo.profileImageUri
        imageUri = Uri.parse(imageUriString)
        if (imageUriString.isNotEmpty()) {
            Glide.with(this)
                .load(imageUri)
                .into(imageProfilePicture)
        }
    }

    /**
     * Save login info to shared preferences and user info to the database
     * Information saved: id (PK), username, email, profile picture Uri as string
     */
    private fun updateUserInformation(): Boolean {
        val username = textInputUsername.editText?.text.toString()
        val password = textInputPassword.editText?.text.toString()
        val email = textInputEmail.editText?.text.toString()

        return if (!validateFields()) {
            false
        } else {
            // update login info on shared preferences
            val editor = sharedPref.edit()
            editor.apply {
                putString(username.plus(R.string.pass_key), password)
                if (currentUser != username) {
                    putString(username.plus(R.string.user_key), username)
                    remove(currentUser.plus(R.string.user_key))
                    remove(currentUser.plus(R.string.pass_key))
                    putString(getString(R.string.current_user_key), username)
                }
            }.apply()

            // update user on the database
            val updatedUser = User(args.currentUserInfo.id, username, email, imageUri.toString())
            mUserViewModel.updateUser(updatedUser)

            currentUser = username

            textInputUsername.error = ""
            textInputPassword.error = ""
            textInputEmail.error = ""
            true
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
            (sharedPref.contains(username.plus(R.string.user_key)) and (username != currentUser)) -> {
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
        val listOfEmails: MutableList<String> = mutableListOf()
        mUserViewModel.readAllEmails.observe(viewLifecycleOwner, { emails ->
            for (i in emails)
                listOfEmails.add(i)
        })
        return when {
            email.isEmpty() -> {
                textInputEmail.error = "Field can't be empty"
                false
            }
            (email in listOfEmails) and (email != args.currentUserInfo.email) -> {
                textInputEmail.error = "Email already registered to another account"
                false
            }
            else -> {
                textInputEmail.error = ""
                true
            }
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
                .into(binding.imageProfilePictureProfile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}