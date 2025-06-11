package com.example.suntrackir

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.suntrackir.ui.SunTrackirDashboard
import com.example.suntrackir.viewmodel.SunMoonViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.threeten.bp.LocalDate

class MainActivity : ComponentActivity() {
    private val viewModel: SunMoonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SunTrackirDashboard(
                viewModel = viewModel,
                onShowDatePicker = { showDatePicker() },
                onShowSettings = { showSettingsBottomSheet() },
                onRequestGps = { callback ->
                    getGpsLocation { lat, lon ->
                        callback(lat, lon)
                    }
                }
            )
        }
    }

    private fun showSettingsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_settings, null)
        dialog.setContentView(view)

        val editLatitude = view.findViewById<android.widget.EditText>(R.id.editLatitude)
        val editLongitude = view.findViewById<android.widget.EditText>(R.id.editLongitude)
        editLatitude.setText(viewModel.latitude.toString())
        editLongitude.setText(viewModel.longitude.toString())

        view.findViewById<android.widget.Button>(R.id.btnSaveCoordinates).setOnClickListener {
            val lat = editLatitude.text.toString().toDoubleOrNull()
            val lon = editLongitude.text.toString().toDoubleOrNull()
            if (lat != null && lon != null) {
                viewModel.updateLocation(lat, lon)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "مختصات معتبر وارد کنید", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<android.widget.Button>(R.id.btnGetGps).setOnClickListener {
            getGpsLocation { lat, lon ->
                viewModel.updateLocation(lat, lon)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                viewModel.setDate(LocalDate.of(year, month + 1, day))
            },
            viewModel.currentDate.year,
            viewModel.currentDate.monthValue - 1,
            viewModel.currentDate.dayOfMonth
        ).show()
    }

    private fun getGpsLocation(onLocationReady: (Double, Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = LocationManager.GPS_PROVIDER
        val location: Location? = lm.getLastKnownLocation(provider)
        if (location != null) {
            onLocationReady(location.latitude, location.longitude)
        } else {
            Toast.makeText(this, "دریافت مختصات GPS ممکن نشد", Toast.LENGTH_SHORT).show()
        }
    }
}