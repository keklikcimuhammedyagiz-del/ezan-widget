package com.ezan.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvLocation: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvNextName: TextView
    private lateinit var tvNextTime: TextView
    private lateinit var timesContainer: LinearLayout
    private lateinit var tvNote: TextView

    private var lat = 41.01
    private var lng = 28.98
    private var city = "İstanbul"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLocation = findViewById(R.id.tvLocation)
        tvDate = findViewById(R.id.tvDate)
        tvNextName = findViewById(R.id.tvNextName)
        tvNextTime = findViewById(R.id.tvNextTime)
        timesContainer = findViewById(R.id.timesContainer)
        tvNote = findViewById(R.id.tvNote)

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            requestLocationAndUpdate()
        }

        loadSaved()
        updateUI()
        requestLocationAndUpdate()
    }

    private fun loadSaved() {
        val prefs = getSharedPreferences("ezan", Context.MODE_PRIVATE)
        lat = prefs.getFloat("lat", 41.01f).toDouble()
        lng = prefs.getFloat("lng", 28.98f).toDouble()
        city = prefs.getString("city", "İstanbul") ?: "İstanbul"
    }

    private fun save() {
        getSharedPreferences("ezan", Context.MODE_PRIVATE).edit()
            .putFloat("lat", lat.toFloat())
            .putFloat("lng", lng.toFloat())
            .putString("city", city)
            .apply()
        // Widget'ları da güncelle
        val mgr = android.appwidget.AppWidgetManager.getInstance(this)
        val ids = mgr.getAppWidgetIds(
            android.content.ComponentName(this, PrayerWidget::class.java)
        )
        for (id in ids) {
            PrayerWidget.updateAppWidget(this, mgr, id)
        }
    }

    private fun requestLocationAndUpdate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                100
            )
            return
        }
        try {
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc: Location? = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (loc != null) {
                lat = loc.latitude
                lng = loc.longitude
                resolveCity()
            } else {
                tvNote.text = "Konum alınamadı, varsayılan (İstanbul) kullanılıyor."
            }
        } catch (e: Exception) {
            tvNote.text = "Konum hatası: ${e.message}"
        }
        save()
        updateUI()
    }

    private fun resolveCity() {
        try {
            val geocoder = Geocoder(this, Locale("tr"))
            val list = geocoder.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                val a = list[0]
                city = a.subAdminArea ?: a.adminArea ?: a.locality ?: "Konum"
            }
        } catch (_: Exception) {
            city = "Konum (%.2f, %.2f)".format(lat, lng)
        }
    }

    private fun updateUI() {
        tvLocation.text = city
        val now = Calendar.getInstance()
        tvDate.text = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")).format(now.time)

        val times = PrayerCalc.calculate(lat, lng)
        val names = PrayerCalc.names()
        val next = PrayerCalc.nextIndex(times)

        tvNextName.text = names[next]
        tvNextTime.text = PrayerCalc.format(times[next])

        timesContainer.removeAllViews()
        for (i in names.indices) {
            val row = layoutInflater.inflate(android.R.layout.simple_list_item_2, timesContainer, false)
            val t1 = row.findViewById<TextView>(android.R.id.text1)
            val t2 = row.findViewById<TextView>(android.R.id.text2)
            t1.setTextColor(0xFFEEf5f1.toInt())
            t2.setTextColor(0xFF9bbcae.toInt())
            t1.text = names[i]
            t2.text = PrayerCalc.format(times[i])
            if (i == next) {
                row.setBackgroundColor(0xFF2e8b6a.toInt())
            }
            timesContainer.addView(row)
        }
        tvNote.text = "Kaynak: Cihaz hesabı (yaklaşık). Widget ana ekrana eklenebilir."
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationAndUpdate()
        } else {
            Toast.makeText(this, "Konum izni verilmedi, varsayılan kullanılıyor", Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }
}
