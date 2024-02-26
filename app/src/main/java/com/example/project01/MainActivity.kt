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
import org.w3c.dom.Text
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()
    private val url = "https://api.open-meteo.com/v1/forecast"

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100 // Define your request code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userLocationTxtBx = findViewById<TextView>(R.id.txtBxLocation)
        var location = arrayListOf<Double>()

        findViewById<Button>(R.id.btnUseMyLocation).setOnClickListener() {
            // If the permission is not granted then ask for it
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showToast("Location is not enabled")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_CODE)
            } else {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                getLastLocation { latitude, longitude ->
                    location.add(latitude)
                    location.add(longitude)
                    userLocationTxtBx.setText(getAreaName(latitude, longitude))
                }
            }
        }

        findViewById<Button>(R.id.btnSubmitInformation).setOnClickListener() {
            val intent = Intent(applicationContext, WeatherInformation::class.java)
            Helper.processLocation(this, intent, location)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation(callback: (latitude: Double, longitude: Double) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Got last known location. In some rare situations, this can be null.
                if (location != null) {
                    // Handle location data
                    val latitude = location.latitude
                    val longitude = location.longitude
                    callback(latitude, longitude)
                } else {
                    callback(Double.NaN, Double.NaN) // Pass NaN values to indicate failure
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
                showToast("Error: ${e.message}")
                callback(Double.NaN, Double.NaN)
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
