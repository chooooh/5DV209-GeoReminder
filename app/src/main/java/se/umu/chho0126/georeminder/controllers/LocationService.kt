package se.umu.chho0126.georeminder.controllers

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.NOTIFICATION_CHANNEL_ID
import se.umu.chho0126.georeminder.models.Position
import java.util.*

private const val TAG = "LocationService"
class LocationService : LifecycleService(){
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private var locationCallback: LocationCallback = MyLocationCallback()
    private var mapRepository: MapRepository = MapRepository.get()
    private var positionsLiveData: LiveData<List<Position>> = mapRepository.getPositions()
    private var positions: List<Position> = listOf()

    private inner class MyLocationCallback: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation = locationResult.lastLocation
            var dist: Float = 0F

            positions.forEach {
                val location = Location("")
                location.latitude = it.latitude
                location.longitude = it.longitude
                val distance = currentLocation.distanceTo(location)
                Log.d(TAG, "distance from your location to ${it.title} is $distance")
                dist = distance
            }
            val notification = createNotification("Location Service", "distance to latest: $dist")
            startForeground(2, notification)
            Log.d(TAG, "${locationResult.lastLocation}")
            Log.d(TAG, "${positions}")

        }
    }
    private fun createNotification(title: String, description: String): Notification {
        val notification = NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .build()

        return notification
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val startActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0)

        // Lägg strängar i resources
        val notification = createNotification("Location Service", "Tracking location")

        startForeground(1, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = createLocationRequest(10000, 5000)
        if(checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        positionsLiveData.observe(this) {
            positions = it
        }

        return START_NOT_STICKY
    }

    private fun checkLocationPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return permission
    }

    private fun createLocationRequest(interval: Int, fastestInterval: Int, priority: Int = LocationRequest.PRIORITY_HIGH_ACCURACY): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

}