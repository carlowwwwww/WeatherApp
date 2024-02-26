package com.example.project01

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

class DataAdapter {
    @JsonClass(generateAdapter = true)
    data class CurrentUnits(
        val time: String,
        val interval: String,
        @Json(name = "temperature_2m") val temperature2m: String,
        @Json(name = "wind_speed_10m") val windSpeed10m: String
    )

    @JsonClass(generateAdapter = true)
    data class Current(
        val time: String,
        val interval: Int,
        @Json(name = "temperature_2m") val temperature2m: Double,
        @Json(name = "wind_speed_10m") val windSpeed10m: Double
    )

    @JsonClass(generateAdapter = true)
    data class WeatherData(
        val latitude: Double,
        val longitude: Double,
        val generationtime_ms: Double,
        val utc_offset_seconds: Int,
        val timezone: String,
        val timezone_abbreviation: String,
        val elevation: Int,
        @Json(name = "current_units") val currentUnits: CurrentUnits,
        val current: Current
    )
}