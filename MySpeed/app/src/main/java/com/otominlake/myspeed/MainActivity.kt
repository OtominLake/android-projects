package com.otominlake.myspeed

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.otominlake.myspeed.databinding.ActivityMainBinding
import java.text.DecimalFormat


// Constants
const val LOG_TAG = "MySpeedLogTag"
const val LOCATION_INTERVAL: Long = 1000
const val MIN_UPDATES = 3
const val MILLI_IN_SECOND = 1000L
const val MILLI_IN_MINUTE = MILLI_IN_SECOND * 60
const val MILLI_IN_HOUR = MILLI_IN_MINUTE * 60
const val MPS_TO_KMH = 3.6
const val SPEED_THRESHOLD = 4.0

class MainActivity : AppCompatActivity() {
    // View binding
    private lateinit var binding: ActivityMainBinding

    // Location objects
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Variables to be used to display data
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var distance: Double = 0.0
    private var speed: Double = 0.0
    private var avgSpeed: Double = 0.0
    private var maxSpeed: Double = 0.0
    private var recentTime: Long = 0
    private var timeElapsed = 0L

    // Keep number of updates to skip first ones while the accuracy is setting up
    private var updates = 0

    // Precisions for conversion
    private val dfSpeed = DecimalFormat("00.0")
    private val dfDistanceMeters = DecimalFormat("0")
    private val dfDistanceKilometers = DecimalFormat("0.00")
    private val dfTwoDigits = DecimalFormat("00")

    // Set up everything
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Configure location request
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = LOCATION_INTERVAL
        locationRequest.fastestInterval = LOCATION_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (savedInstanceState != null) {
            distance = savedInstanceState.getDouble("distance")
            updates = savedInstanceState.getInt("updates")
            maxSpeed = savedInstanceState.getDouble("maxSpeed")
            latitude = savedInstanceState.getDouble("latitude")
            longitude = savedInstanceState.getDouble("longitude")
            recentTime = savedInstanceState.getLong("timer")
            timeElapsed = savedInstanceState.getLong("timeElapsed")
        }

        updateUI()

        // Define a location callback function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
//                Log.d(LOG_TAG,"onLocationResult")
                locationUpdate(locationResult)
            }
        }

        // Handle reset button
        binding.mainBtReset.setOnClickListener {
            distance = 0.0
            timeElapsed = 0L
            updates = 0
            maxSpeed = 0.0
            avgSpeed = 0.0
            updateUI()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putDouble("distance", distance)
        outState.putInt("updates", updates)
        outState.putDouble("maxSpeed", maxSpeed)
        outState.putDouble("latitude", latitude)
        outState.putDouble("longitude", longitude)
        outState.putLong("timer", recentTime)
        outState.putLong("timeElapsed", timeElapsed)
    }

    // Start location updates
    override fun onResume() {
        super.onResume()
//        Log.d(LOG_TAG,"onResume")
        startLocationUpdates()
    }

    // Stop location updates
    override fun onPause() {
        super.onPause()
//        Log.d(LOG_TAG,"onPause")
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
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No permissions - request and wait for onRequestPermissionsResult, then
            // if granted, call this function again
            Log.e(LOG_TAG, "No permissions to gain location")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
//        Log.d(LOG_TAG, "startLocationUpdates")
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
        for (location in locationResult.locations){
            speed = location.speed.toDouble() * MPS_TO_KMH
            val newTime = System.currentTimeMillis()

            if (speed > SPEED_THRESHOLD) {

                if (updates == MIN_UPDATES) {
                    // distance and time measurement starts here
                    distance = 0.0
                    timeElapsed = 0L
                }
                else if (updates > MIN_UPDATES) {
                    val distanceCalculated = floatArrayOf(0f)
                    Location.distanceBetween(
                        latitude,
                        longitude,
                        location.latitude,
                        location.longitude,
                        distanceCalculated
                    )

                    distance += distanceCalculated[0]
                    timeElapsed += newTime - recentTime
                    avgSpeed = distance / timeElapsed * MILLI_IN_SECOND * MPS_TO_KMH
                    if (maxSpeed < speed) {
                        maxSpeed = speed
                    }

                }
            }
            // Update UI
            updateUI()

            latitude = location.latitude
            longitude = location.longitude
            recentTime = newTime
            updates++

//            Log.i(LOG_TAG,"location callback lat $latitude, lon $longitude, time $timeElapsed")
        }
    }

    private fun updateUI() {
        binding.mainTvSpeed.text = dfSpeed.format(speed)
        binding.mainTvDistance.text = convertDistance()
        binding.mainTvTime.text = resources.getString(R.string.time_text, convertTime())
        binding.mainTvMaxSpeed.text = resources.getString(R.string.max_speed_text, dfSpeed.format(maxSpeed))
        binding.mainTvAvgSpeed.text = resources.getString(R.string.avg_speed_text, dfSpeed.format(avgSpeed))
    }

    // Show distance in meters or kilometers
    private fun convertDistance() : String {
        if (distance < 1000.0) {
             return dfDistanceMeters.format(distance) + "m"
        }
        return dfDistanceKilometers.format(distance / 1000.0) + "km"
    }

    // Show time in hours, minutes and seconds
    private fun convertTime() : String {
        var time = timeElapsed

        if (time >= 0) {
            val hours = time / MILLI_IN_HOUR
            time -= hours * MILLI_IN_HOUR
            val minutes = time / MILLI_IN_MINUTE
            time -= minutes * MILLI_IN_MINUTE
            val seconds = time / MILLI_IN_SECOND

            return when {
                hours > 0 -> {
                    "$hours:${dfTwoDigits.format(minutes)}:${dfTwoDigits.format(seconds)}"
                }
                minutes > 0 -> {
                    "${minutes}min ${dfTwoDigits.format(seconds)}s"
                }
                else -> {
                    "${seconds}s"
                }
            }
        } else
        {
            return "--"
        }
    }
}
