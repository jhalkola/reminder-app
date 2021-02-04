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


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val pickImage = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.pfpImage.clipToOutline = true
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
        val username = binding.textUsernameRegister.text.toString()
        val password = binding.textPasswordRegister.text.toString()
        val email = binding.textEmailRegister.text.toString()
        if (username.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty()) {
            val sharedPref = (activity as Context).applicationContext.getSharedPreferences(
                getString(R.string.sharedPref),
                Context.MODE_PRIVATE
            )
            when {
                sharedPref.contains(username.plus("UsernameKey")) -> {
                    val toast = Toast.makeText(activity as Context, "Username already taken", Toast.LENGTH_SHORT)
                    toast.show()
                    return false
                }
                sharedPref.contains(email.plus("EmailKey")) -> {
                    val toast = Toast.makeText(activity as Context, "Email already in use on another account", Toast.LENGTH_SHORT)
                    toast.show()
                    return false
                }
                else -> {
                    val editor = sharedPref.edit()
                    editor.apply {
                        putString(username.plus("UsernameKey"), username)
                        putString(password.plus("PasswordKey"), password)
                        putString(email.plus("EmailKey"), email)
                    }.apply()
                    return true
                }
            }
        }
        else {
            val toast = Toast.makeText(activity as Context, "Cannot register with empty fields!", Toast.LENGTH_SHORT)
            toast.show()
            return false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImage && resultCode == Activity.RESULT_OK) {
            val imageURI = data?.data
            binding.pfpImage.setImageURI(imageURI)
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, pickImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}