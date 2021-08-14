package com.example.FlightMobileApp

import com.google.gson.annotations.SerializedName

/**
 * Command class.
 * serialized to json format
 */
data class Command (
    @SerializedName("aileron") var aileron: Double,
    @SerializedName("rudder") var rudder: Double,
    @SerializedName("elevator") var elevator: Double,
    @SerializedName("throttle") var throttle: Double
)