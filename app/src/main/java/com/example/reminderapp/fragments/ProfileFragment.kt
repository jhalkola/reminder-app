package com.example.reminderapp.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reminderapp.R
import com.example.reminderapp.activities.LoginActivity
import com.example.reminderapp.databinding.FragmentProfileBinding
import com.example.reminderapp.db.entities.User
import com.example.reminderapp.db.viewmodels.UserViewModel
import com.google.android.material.textfield.TextInputLayout

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputPassword: TextInputLayout
    private lateinit var textInputEmail: TextInputLayout
    private lateinit var imageProfilePicture: ImageView
    private lateinit var sharedPref: SharedPreferences
    private lateinit var currentUser: String
    private lateinit var currentUserInfo: User
    private lateinit var mUserViewModel: UserViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.imageProfilePictureProfile.clipToOutline = true

        // get shared preferences
        sharedPref = (activity as Context).applicationContext.getSharedPreferences(
                getString(R.string.sharedPref), Context.MODE_PRIVATE)
        currentUser = sharedPref.getString(getString(R.string.current_user_key), null).toString()

        textInputUsername = binding.textFieldUsernameProfile
        textInputPassword = binding.textFieldPasswordProfile
        textInputEmail = binding.textFieldEmailProfile
        imageProfilePicture = binding.imageProfilePictureProfile

        // set view model for accessing database
        mUserViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        loadUserInfo()


        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(false)
        supActionBar.title = "Profile"
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonLogout.setOnClickListener {
            setLoginStatus()
            val logoutIntent = Intent(activity as Context, LoginActivity::class.java)
            startActivity(logoutIntent)
        }
        binding.buttonEditProfile.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToProfileEditFragment(currentUserInfo)
            findNavController().navigate(action)
        }
    }

    private fun loadUserInfo() {
        val savedPassword = sharedPref.getString(currentUser.plus(R.string.pass_key), null)
        textInputPassword.editText?.setText(savedPassword)

        mUserViewModel.readUser(currentUser).observe(viewLifecycleOwner, { user ->
            textInputUsername.editText?.setText(user.username)
            textInputEmail.editText?.setText(user.email)
            val imageUri = Uri.parse(user.profileImageUri)
            if (imageUri.toString().isNotEmpty()) {
                Glide.with(this)
                        .load(imageUri)
                        .into(imageProfilePicture)
            }
            currentUserInfo = user
        })
    }

    private fun setLoginStatus() {
        sharedPref.edit().putInt(getString(R.string.login_key), 0).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}