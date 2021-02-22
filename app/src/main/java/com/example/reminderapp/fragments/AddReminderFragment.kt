package com.example.reminderapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentReminderBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.viewmodels.ReminderViewModel
import java.time.LocalDateTime
import java.time.Month


class AddReminderFragment : Fragment() {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val pickImage = 1
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var imageUri: Uri


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)

        sharedPref = (activity as Context).applicationContext.getSharedPreferences(
            getString(R.string.sharedPref), Context.MODE_PRIVATE)

        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(true)
        supActionBar.title = ""

        // set default image to empty in case no profile picture is chosen
        imageUri = Uri.parse("")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imageReminder.setOnLongClickListener {
            deleteImage()
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        menu.findItem(R.id.deleteReminder).isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            saveReminder()
            findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment)
            true
        }
        R.id.confirmReminder -> {
            if (saveReminder()) { findNavController().navigate(R.id.action_addReminderFragment_to_homeFragment) }
            true
        }
        R.id.setNotificationReminder -> {
            TODO()
        }
        R.id.addImage -> {
            pickImage()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun saveReminder(): Boolean {
        val message = binding.textMessage.text.toString()
        val image = imageUri.toString()
        val locationX = 0.0
        val locationY = 0.0
        val reminderTime = LocalDateTime.of(2021, Month.FEBRUARY, 20, 14, 0).toString()
        val creationTime = LocalDateTime.now().toString()
        val creatorID = sharedPref.getInt(getString(R.string.current_user_id_key), 0)
        val reminderSeen = false

        val reminder = Reminder(0,
                message,
                image,
                locationX,
                locationY,
                reminderTime,
                creationTime,
                creatorID,
                reminderSeen
        )
        return if (message.isNotEmpty()) {
            mReminderViewModel.addReminder(reminder)
            true
        } else {
            Toast.makeText(requireContext(), "Message field cannot be empty. Reminder not saved", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun deleteImage() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_, _ ->
            imageUri = Uri.parse("")
            Glide.with(this)
                    .clear(binding.imageReminder)
        }
        builder.setNegativeButton("No") {_, _ -> }
        builder.setTitle("Remove image?")
        builder.create().show()
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
                    .into(binding.imageReminder)
        }
    }
}
