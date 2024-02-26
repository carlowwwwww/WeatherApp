package com.example.project01

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class WeatherInformation : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_information)

        val informationField = findViewById<TextView>(R.id.information)
        val dateTime = intent.getStringExtra("LAST_UPDATED")?.split("T")

        informationField.text = "Area: ${intent.getStringExtra("AREA")}\n" +
                                "Temperature: ${intent.getDoubleExtra("TEMP", 0.0)} °C\n" +
                                "Wind speed: ${intent.getDoubleExtra("WIND_SPEED", 0.0)} km/h\n\n" +
                                "Last Updated:\n${dateTime?.get(0)} ${dateTime?.get(1)}\n\n\n" +
                                "This information will be updated every 15 minutes."


        findViewById<Button>(R.id.btnChangeLoc).setOnClickListener() {
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
    }
}