package com.example.project01

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class Helper {
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100 // Define your request code
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private val client = OkHttpClient()
        private const val url = "https://api.open-meteo.com/v1/forecast"

    fun processLocation(context: Context, intent: Intent, location: ArrayList<Double>) {
        if (location.isNotEmpty()) {
            val request = Request.Builder()
                .url("$url?latitude=${location[0]}&longitude=${location[1]}&current=temperature_2m,wind_speed_10m&timezone=auto")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast(context, "Network connection is needed")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { responseBody ->
                        try {
                            val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
                            val adapter: JsonAdapter<DataAdapter.WeatherData> =
                                moshi.adapter(DataAdapter.WeatherData::class.java)
                            val weatherData: DataAdapter.WeatherData? =
                                adapter.fromJson(responseBody.source())

                            val roundedTemperature =
                                weatherData?.current?.temperature2m?.let { temperature ->
                                    kotlin.math.ceil(temperature)
                                }

                            intent.putExtra("AREA", weatherData?.timezone)
                            intent.putExtra("LAST_UPDATED", weatherData?.current?.time)
                            intent.putExtra("TEMP", roundedTemperature)
                            intent.putExtra("WIND_SPEED", weatherData?.current?.windSpeed10m)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            showToast(
                                context,
                                "Cannot access the server at the moment. Please try again later"
                            )
                        }
                    }
                }
            })
        } else {
            showToast(context, "No location selected")
        }
    }
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


}