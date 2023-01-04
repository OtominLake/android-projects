package com.otominlake.fluenttraffic

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.DecimalFormat

const val BEARING_SEGMENT = 45
const val BEARING_HALF_SEGMENT = BEARING_SEGMENT / 2

class MyTrafficLightsItemRecyclerViewAdapter(
    private val values: List<TrafficLightsData>
) : RecyclerView.Adapter<MyTrafficLightsItemRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    private val dfDistanceKilometers = DecimalFormat("0.00km")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_traffic_lights_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("am-tag", "Values have ${values.size} elements")
        val trafficLights = values[position]
        val tvName = holder.itemView.findViewById(R.id.tv_name) as TextView
        tvName.text = trafficLights.name
        val tvDistance = holder.itemView.findViewById(R.id.tv_distance) as TextView
        tvDistance.text = dfDistanceKilometers.format(trafficLights.distance)
        val tvBearing = holder.itemView.findViewById(R.id.tv_bearing) as TextView
        tvBearing.text = bearingAsString(trafficLights.bearing)

    }

    override fun getItemCount(): Int = values.size

    fun bearingAsString(bearing: Int): String {
        val arrow = when {
            bearing < 0 -> "?"
            bearing < BEARING_HALF_SEGMENT -> "↑"
            bearing < (BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "↗"
            bearing < (2 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "→"
            bearing < (3 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "↘"
            bearing < (4 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "↓"
            bearing < (5 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "↙"
            bearing < (6 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "←"
            bearing < (7 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "↖"
            bearing < (8 * BEARING_SEGMENT + BEARING_HALF_SEGMENT) -> "↑"
            else -> "?"
        }
        return "$arrow (${bearing.toInt()})"
    }
}