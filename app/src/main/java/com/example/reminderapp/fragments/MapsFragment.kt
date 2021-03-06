package com.example.reminderapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reminderapp.R
import com.example.reminderapp.activities.MainActivity
import com.example.reminderapp.databinding.FragmentMapsBinding
import com.example.reminderapp.db.entities.Reminder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<MapsFragmentArgs>()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminder: Reminder
    private var locationX: Double? = null
    private var locationY: Double? = null
    private var locationChosen = false
    private var locationMoved = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        val supActionBar = (activity as AppCompatActivity).supportActionBar!!
        supActionBar.setDisplayHomeAsUpEnabled(true)
        supActionBar.title = ""

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        reminder = args.currentReminder
        locationX = reminder.location_x
        locationY = reminder.location_y

        hasLocationMoved()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        if (!isLocationPermissionGranted()) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            Log.d("maps", "request permissions")
            requestPermissions(
                permissions.toTypedArray(),
                REQUEST_LOCATION_CODE
            )
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            map.isMyLocationEnabled = true
            if (locationX == null && locationY == null) {
                fusedLocationClient.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        with(map) {
                            // move to last known location
                            val latlng = LatLng(it.latitude, it.longitude)
                            moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, ZOOM_LEVEL))
                        }
                    } else {
                        // if no last known location move to Vaasa
                        with(map) {
                            val latLng = LatLng(63.093, 21.620)
                            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL))
                        }
                    }
                }
            } else {
                // move to reminder location
                toReminderLocation(map)
            }
        }
        onLongClick(map)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.maps_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        android.R.id.home -> {
            if (locationChosen) {
                returnChosenLocation()
            } else {
                val action = MapsFragmentDirections.actionMapsFragmentToAddReminderFragment(reminder)
                findNavController().navigate(action)
                Toast.makeText(activity,
                        "Reminder location was not chosen",
                        Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
        R.id.saveCoordinates -> {
            if (locationChosen) {
                returnChosenLocation()
            } else {
                Toast.makeText(activity,
                        "Choose location by long clicking a place on the map",
                        Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
        R.id.reminderLocation -> {
            if (locationX != null && locationY != null) {
                toReminderLocation(map)
            } else {
                Toast.makeText(activity,
                        "You have not set reminder location",
                        Toast.LENGTH_SHORT
                ).show()
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun returnChosenLocation() {
        reminder.location_x = locationX
        reminder.location_y = locationY
        val action = if (args.destination == "addFragment") {
            MapsFragmentDirections.actionMapsFragmentToAddReminderFragment(reminder)
        } else {
            MapsFragmentDirections.actionMapsFragmentToEditReminderFragment(reminder)
        }
        findNavController().navigate(action)
        if (locationMoved) {
            Toast.makeText(activity,
                    "Reminder location has been saved",
                    Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hasLocationMoved() {
        if (locationX != null && locationY != null) {
            // location was already chosen and has not been changed
            if (reminder.location_x == locationX && reminder.location_y == locationY) {
                locationChosen = true
            } else {
                locationMoved = true
            }
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity as Context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && (
                            grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                                grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ) {
                    if (ContextCompat.checkSelfPermission(requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        map.isMyLocationEnabled = true
                        onMapReady(map)
                    }
                } else {
                    Toast.makeText(activity, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }

        }
    }

    private fun toReminderLocation(map: GoogleMap) {
        val latlng = LatLng(locationX!!, locationY!!)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, ZOOM_LEVEL))
        map.clear()
        map.addMarker(
                MarkerOptions().position(latlng).title("Reminder location")
        ).showInfoWindow()

        map.addCircle(
                CircleOptions()
                        .center(latlng)
                        .strokeColor(Color.argb(50, 70, 70, 70))
                        .fillColor(Color.argb(70, 150, 150, 150))
                        .radius(GEOFENCE_RADIUS.toDouble())
        )
    }

    private fun onLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener {
            map.clear()
            map.addMarker(
                MarkerOptions().position(it).title("Reminder location")
            ).showInfoWindow()

            map.addCircle(
                CircleOptions()
                    .center(it)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(70, 150, 150, 150))
                    .radius(GEOFENCE_RADIUS.toDouble())
            )

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, ZOOM_LEVEL))

            locationX = it.latitude
            locationY = it.longitude
            locationChosen = true
            locationMoved = true
        }
    }

    companion object {
        private const val REQUEST_LOCATION_CODE = 1
        private const val GEOFENCE_LOCATION_REQUEST_CODE = 2
        private const val ZOOM_LEVEL = 15f
        private const val GEOFENCE_RADIUS = 150
        private const val GEOFENCE_ID = "reminder_geofence_id"
        private const val GEOFENCE_EXPIRATION = 10 * 24 * 60 * 60 * 1000 // 10 days
        private const val GEOFENCE_DWELL_DELAY = 10 * 1000 // 10 sec
    }
}