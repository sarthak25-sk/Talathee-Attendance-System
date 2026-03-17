package com.example.talathiattendance.data.models


data class LocationDetails(
    val lat: Double? = 0.0, val long: Double? = 0.0
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "lat" to lat,
            "long" to long
        )
    }
}
