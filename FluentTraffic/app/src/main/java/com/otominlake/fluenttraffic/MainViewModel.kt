package com.otominlake.fluenttraffic

import android.util.Log
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var trafficLightsList = mutableListOf(
        TrafficLightsData("Kadetów od obwodnicy", 18.50218, 54.37738),
        TrafficLightsData("Kadetów od lotniska", 18.50194, 54.37727),
        TrafficLightsData("Budowlanych od obwodnicy", 18.50860, 54.37660),
        TrafficLightsData("Budowlanych od lotniska", 18.50837, 54.37652),
        TrafficLightsData("Radarowa od obwodnicy", 18.48768, 54.37910),
        TrafficLightsData("Radarowa od Klukowa", 18.48753, 54.37930),
        TrafficLightsData("Przytulna od osiedla", 18.53498, 54.34823),
        TrafficLightsData("Przytulna od Armii Krajowej", 18.53490, 54.34788),
        TrafficLightsData("Przytulna od Auchaun", 18.53458, 54.34807)
    )

    init {
        Log.d("am-tag", "ViewModel started")
    }

    // Update distance to all lights and sort from the closest ones
    fun updateDistanceAndBearing(longitude: Double, latitude: Double, bearing: Float) {
        trafficLightsList.forEach { it.updateDistanceAndBearing(longitude, latitude, bearing) }
        trafficLightsList.sortBy { it.distance }
    }

}