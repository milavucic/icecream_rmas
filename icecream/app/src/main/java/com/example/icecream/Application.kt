package com.example.icecream

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.example.icecream.navigation.Nav
import com.example.icecream.services.LocationService
import com.example.icecream.viewmodels.AuthViewModel
import com.example.icecream.viewmodels.IcecreamViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Application(
    viewModel: AuthViewModel,
    icecreamViewModel: IcecreamViewModel
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val isTrackingServiceEnabled = sharedPreferences.getBoolean("tracking_location", true)

    // Check for location permissions
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Request permissions if they are not granted
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1 // Request code
        )
    } else {
        // Start LocationService when permissions are already granted
        val intent = Intent(context, LocationService::class.java).apply {
            // Set the action based on the tracking setting
            action = if (isTrackingServiceEnabled) {
                LocationService.ACTION_FIND_NEARBY
            } else {
                LocationService.ACTION_START
            }
        }

        // Properly start the service as a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Log.d("LocationService", "Service started with action: ${intent.action}")
    }

    // Main UI surface
    Surface(modifier = Modifier.fillMaxSize()) {
        Nav(viewModel, icecreamViewModel)
    }
}
