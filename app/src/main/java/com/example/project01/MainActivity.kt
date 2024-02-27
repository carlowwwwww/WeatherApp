package com.example.project01

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import android.location.Geocoder
import android.widget.TextView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.w3c.dom.Text
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()
    private val url = "https://api.open-meteo.com/v1/forecast"
    private val location = arrayOf(0.0, 0.0)

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100 // Define your request code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnSelectLocation).setOnClickListener() {
            // If the permission is not granted then ask for it
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showToast("Location is not enabled")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
            } else {
                val intent = Intent(applicationContext, MapsActivity::class.java)
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                getLastLocation { latitude, longitude ->
                    intent.putExtra("LAT", latitude)
                    intent.putExtra("LONG", longitude)
                    startActivity(intent)
                }
            }
        }

        findViewById<Button>(R.id.btnUseMyLocation).setOnClickListener() {
            // If the permission is not granted then ask for it
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showToast("Location is not enabled")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
            } else {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                getLastLocation { latitude, longitude ->
                    location[0] = latitude
                    location[1] = longitude
                }
            }
        }

        findViewById<Button>(R.id.btnSubmitInformation).setOnClickListener() {
            val intent = Intent(applicationContext, WeatherInformation::class.java)
            processLocation(this, intent, location)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(onLocationReady: (Double, Double) -> Unit) {
        val userLocationTxtBx = findViewById<TextView>(R.id.txtBxLocation)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                // Got last known location. In some rare situations, this can be null.
                if (loc != null) {
                    // Handle location data
                    val latitude = loc.latitude
                    val longitude = loc.longitude

                    onLocationReady(latitude, longitude)
                    userLocationTxtBx.setText(getAreaName(latitude, longitude))
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                showToast("Error: ${e.message}")
            }
    }

    private fun getAreaName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            return addresses[0].subAdminArea
        } else {
            "Unknown Area"
        }
    }

    private fun processLocation(context: Context, intent: Intent, location: Array<Double>) {
        if (location[0] != 0.0 && location[1] != 0.0) {
            val request = Request.Builder()
                .url("${url}?latitude=${location[0]}&longitude=${location[1]}&current=temperature_2m,wind_speed_10m&timezone=auto")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    showToast("Network connection is needed ")
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
                            showToast("Cannot access the server at the moment. Please try again later")
                        }
                    }
                }
            })
        } else {
            showToast("No location selected")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
