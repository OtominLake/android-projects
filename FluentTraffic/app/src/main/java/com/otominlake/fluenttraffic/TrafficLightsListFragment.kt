package com.otominlake.fluenttraffic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.LayoutDirection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.otominlake.fluenttraffic.databinding.FragmentTrafficLightsListBinding
import java.text.SimpleDateFormat
import java.util.*


// Constants
const val LOG_TAG = "am-tag"
const val LOCATION_INTERVAL: Long = 1000

/**
 * A fragment representing a list of traffic lights with buttons to log timestamp
 */
class TrafficLightsListFragment : Fragment() {
    private val data : MainViewModel by activityViewModels()
    private lateinit var listTvTime : TextView
    private lateinit var binding: FragmentTrafficLightsListBinding

    // Location objects
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Prepare UI (inflate, bind)
        val view = inflater.inflate(R.layout.fragment_traffic_lights_list, container, false)
        binding = FragmentTrafficLightsListBinding.bind(view)
        binding.listTrafficLights.layoutManager = LinearLayoutManager(context)
        binding.listTrafficLights.adapter = MyTrafficLightsItemRecyclerViewAdapter(data.trafficLightsList)

        binding.listTrafficLights.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        // Configure location request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest()
        locationRequest.interval = LOCATION_INTERVAL
        locationRequest.fastestInterval = LOCATION_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Define a location callback function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d(LOG_TAG,"onLocationResult")
                locationUpdate(locationResult)
            }
        }

        // set timer to show current time
        val mainHandler = Handler(Looper.getMainLooper())
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        mainHandler.postDelayed(object : TimerTask() {
            override fun run() {
                val time = Calendar.getInstance().time

                binding.listTvTime.text = formatter.format(time)
                view.invalidate()
                mainHandler.postDelayed(this, 1000)
            }
        }, 1000)
        return view
    }


    // Start location updates
    override fun onResume() {
        super.onResume()
        Log.d(LOG_TAG,"onResume")
        startLocationUpdates()
    }

    // Stop location updates
    override fun onPause() {
        super.onPause()
        Log.d(LOG_TAG,"onPause")
        stopLocationUpdates()
    }

    // Handle permissions request results. This is delivered asynchronously
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    Log.i(LOG_TAG, "Permission granted")

                    // Permission is granted - start location updates
                    startLocationUpdates()
                }
                else {
                    Log.i(LOG_TAG, "Permission denied")
                }
                return
            }
            else -> {
                Log.e(LOG_TAG, "Permission result: requestedCode=$requestCode")
            }
        }
    }

    // Request location updates. First check for permissions (may lead to async check of permissions)
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No permissions - request and wait for onRequestPermissionsResult, then
            // if granted, call this function again
            Log.e(LOG_TAG, "No permissions to gain location")
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
        Log.d(LOG_TAG, "startLocationUpdates")
    }

    // Stop location updates (lost focus, blurred, etc)
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Got the update
    private fun locationUpdate(locationResult: LocationResult?) {
        locationResult ?: return

        // To display speed we need only the last location but to calculate the distance need
        // to use all locations provided
        val location = locationResult.locations.last()
        Log.i(LOG_TAG,"location callback lon ${location.longitude}, lat ${location.latitude}, bearing {${location.bearing}")
        data.updateDistanceAndBearing(location.longitude, location.latitude, location.bearing)
        binding.listTrafficLights.adapter?.notifyDataSetChanged()
    }
}