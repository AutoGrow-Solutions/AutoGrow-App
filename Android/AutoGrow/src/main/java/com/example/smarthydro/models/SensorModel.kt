package com.example.smarthydro.models


import com.google.gson.annotations.SerializedName

data class SensorModel(
    @SerializedName("EC")
    val eC: String = "",
    @SerializedName("Humidity")
    val humidity: String = "",
    @SerializedName("Light")
    val light: String = "",
    @SerializedName("PH")
    val pH: String = "",
    @SerializedName("Temperature")
    val temperature: String = "",
    @SerializedName("FlowRate")
    val flowRate: String = ""

)