package se.umu.chho0126.georeminder.controllers

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import se.umu.chho0126.georeminder.GeoReminderApplication
import se.umu.chho0126.georeminder.NOTIFICATION_CHANNEL_ID
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import java.util.*

private const val TAG = "LocationService"
class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private var locationCallback: LocationCallback = MyLocationCallback()

    private inner class MyLocationCallback: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation = locationResult.lastLocation
            Log.d(TAG, "${locationResult.lastLocation}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0)

        // Lägg strängar i resources
        val notification = NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Tracking location..")
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .build()

        startForeground(1, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not implemented")
    }
}