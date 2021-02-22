package com.example.reminderapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.reminderapp.R
import com.example.reminderapp.databinding.FragmentReminderBinding
import com.example.reminderapp.db.entities.Reminder
import com.example.reminderapp.db.viewmodels.ReminderViewModel


class EditReminderFragment : Fragment() {
    private var _binding: FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditReminderFragmentArgs>()
    private val pickImage = 1
    private lateinit var mReminderViewModel: ReminderViewModel
    private lateinit var imageUri: Uri

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentReminderBinding.inflate(inflater, container, false)

        mReminderViewModel = ViewModelProvider(this).get(ReminderViewModel::class.java)

        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(true)
        supActionBar.title = ""

        // display reminder data, edit fragment is both showing the reminder and allows editing it
        binding.textMessage.setText(args.currentReminder.message)
        imageUri = Uri.parse(args.currentReminder.imageUri)
        binding.imageReminder.setImageURI(imageUri)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.reminder_toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imageReminder.setOnLongClickListener {
            deleteImage()
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            updateReminder()
            findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment)
            true
        }
        R.id.confirmReminder -> {
            if (updateReminder()) { findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment) }
            true
        }
        R.id.setNotificationReminder -> {
            TODO()
        }
        R.id.addImage -> {
            pickImage()
            true
        }
        R.id.deleteReminder -> {
            deleteReminder()
            true
        }
        else -> super.onOptionsItemSelected(item)
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

    private fun deleteReminder() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mReminderViewModel.deleteReminder(args.currentReminder)
            Toast.makeText(requireContext(), "Reminder removed", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_editReminderFragment_to_homeFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete reminder?")
        builder.setMessage("Are you sure you want to delete this reminder?")
        builder.create().show()
    }

    private fun updateReminder(): Boolean {
        val message = binding.textMessage.text.toString()
        val image = imageUri.toString()

        val reminder = Reminder(
                args.currentReminder.id,
                message,
                image,
                args.currentReminder.location_x,
                args.currentReminder.location_y,
                args.currentReminder.reminder_time,
                args.currentReminder.creation_time,
                args.currentReminder.creator_id,
                args.currentReminder.reminder_seen
        )
        return if (message.isNotEmpty()) {
            mReminderViewModel.updateReminder(reminder)
            true
        } else {
            Toast.makeText(activity as Context, "Message field cannot be empty. Reminder was not changed", Toast.LENGTH_SHORT).show()
            false
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
                    .into(binding.imageReminder)
        }
    }
}