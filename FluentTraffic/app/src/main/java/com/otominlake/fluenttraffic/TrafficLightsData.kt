package com.otominlake.fluenttraffic
import android.location.Location

// Contains data about traffic lights. Displayed in lists and stored to database
class TrafficLightsData(
    private var _name: String,
    private var _longitude: Double,
    private var _latitude: Double
) {
    private var _timestamps = emptyList<Long>()
    private var _distance = 0.0 // km
    private var _bearing = 0 // degrees north oriented

    // Public read only properties
    val name: String
        get() = _name
    val distance: Double
        get() = _distance
    val bearing: Int
        get() = _bearing

    // Update distance based on coordinates
    // bearing is relative to the current movement bearing
    fun updateDistanceAndBearing(longitude: Double, latitude: Double, bearing: Float) {
        // to get distance and initial bearing, you need to pass 2 float array as the last parameter
        val distanceCalculated = floatArrayOf(0f, 0f)
        Location.distanceBetween(
            latitude,
            longitude,
            _latitude,
            _longitude,
            distanceCalculated
        )
        _distance = distanceCalculated[0].toDouble() / 1000
        _bearing = (distanceCalculated[1] - bearing).toInt() % 360
        if (_bearing < 0) {
            _bearing += 360
        }
    }
}