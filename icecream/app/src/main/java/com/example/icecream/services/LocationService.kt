package com.example.icecream.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.icecream.MainActivity
import com.example.icecream.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationService : Service() {
    private val service = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var location: FusedLocationProviderClient
    private val notifiedIc = mutableSetOf<String>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        location = LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d("LocationService", "Service started")
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationUpdates()
            }
            ACTION_STOP -> {
                Log.d("LocationService", "Service stopped")
                stopLocationUpdates()
            }
            ACTION_FIND_NEARBY -> {
                Log.d("NearbyService", "Service started")
                startForeground(NOTIFICATION_ID, createNotification())
                startLocationUpdates(nearby = true)
            }
        }
        return START_NOT_STICKY
    }

    private fun startLocationUpdates(nearby: Boolean = false) {
        getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                Log.d("Location", "${location.latitude} ${location.longitude}")
                broadcastLocationUpdate(location.latitude, location.longitude)
                if (nearby) checkDistanceToIcecream(location.latitude, location.longitude)
            }
            .launchIn(service)
    }

    private fun stopLocationUpdates() {
        stopForeground(true)
        stopSelf()
        service.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        service.cancel()
    }

    private fun getLocationUpdates(interval: Long): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            throw Exception("Missing location permission")
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            throw Exception("GPS is disabled")
        }

        val request = LocationRequest.create()
            .setInterval(interval)
            .setFastestInterval(interval)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.lastOrNull()?.let { location ->
                    launch { send(location) }
                }
            }
        }

        location.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        awaitClose { location.removeLocationUpdates(locationCallback) }
    }

    private fun broadcastLocationUpdate(latitude: Double, longitude: Double) {
        val intent = Intent(ACTION_LOCATION_UPDATE).apply {
            putExtra(EXTRA_LOCATION_LATITUDE, latitude)
            putExtra(EXTRA_LOCATION_LONGITUDE, longitude)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun checkDistanceToIcecream(latitude: Double, longitude: Double) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("icecreams").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val geoPoint = document.getGeoPoint("location")
                    geoPoint?.let {
                        val distance = calculateDistance(latitude, longitude, it.latitude, it.longitude)
                        if (distance <= 100 && !notifiedIc.contains(document.id)) {
                            sendNearbyIcecreamNot()
                            notifiedIc.add(document.id)
                            Log.d("Nearby", "Near icecream: ${document.id}")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationService", "Error fetching", e)
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRad = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val x = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val y = 2 * atan2(sqrt(x), sqrt(1 - x))
        return earthRad * y
    }

    private fun sendNearbyIcecreamNot() {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val not = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Nearby icecream")
            .setContentText("Icecream is near you!")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NEARBY_ICECREAM_NOTIFICATION_ID, not)
    }

    private fun createNotificationChannel() {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Location Tracking",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Tracking your location in the background so you can find your icecream."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification():
            android.app.Notification {
        val notificationChannelId = "LOCATION_SERVICE_CHANNEL"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Location Tracking")
            .setContentText("Location tracking service is running in the background.")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun Context.hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE"
        const val ACTION_FIND_NEARBY = "ACTION_FIND_NEARBY"
        const val EXTRA_LOCATION_LONGITUDE = "EXTRA_LOCATION_LONGITUDE"
        const val EXTRA_LOCATION_LATITUDE = "EXTRA_LOCATION_LATITUDE"
        private const val NOTIFICATION_ID = 1
        private const val NEARBY_ICECREAM_NOTIFICATION_ID = 2
    }
}
