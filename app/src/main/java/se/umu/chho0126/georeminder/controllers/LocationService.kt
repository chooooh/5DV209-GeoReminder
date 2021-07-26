package se.umu.chho0126.georeminder.controllers

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import com.google.android.gms.location.*
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.NOTIFICATION_CHANNEL_ID
import se.umu.chho0126.georeminder.models.Position
import java.util.*

private const val TAG = "LocationService"
private const val NOTIFICATION_REQUEST_CODE = 1
private const val MAIN_PENDING_INTENT_REQUEST_CODE = 0
private const val MAP_FRAGMENT_PENDING_INTENT_REQUEST_CODE = 1

class LocationService : LifecycleService(){
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback = MyLocationCallback()
    private var mapRepository: MapRepository = MapRepository.get()
    private var positionsLiveData: LiveData<List<Position>> = mapRepository.getPositions()
    private var positions: List<Position> = listOf()

    private data class PositionDTO(val position: Position, val distanceTo: Float, val userLocation: Location) {
        override fun toString(): String {
            return "${position.title} - ${distanceTo}"
        }
    }

    private inner class MyLocationCallback: LocationCallback() {
        private var positionsWithinRange: MutableList<PositionDTO> = mutableListOf()
        private fun MutableList<PositionDTO>.containsPosition(position: Position): Boolean {
            return this.any {
                it.position == position
            }
        }

        override fun onLocationResult(locationResult: LocationResult) {
            val currentLocation = locationResult.lastLocation
            var dist: Float = 0F
            positionsWithinRange = mutableListOf()

            positions.forEach {
                val location = Location("")
                location.latitude = it.latitude
                location.longitude = it.longitude
                val distance = currentLocation.distanceTo(location)
                val range = 1000
                if (distance <= range) {
                    val positionDTO = PositionDTO(it, distance, currentLocation)
                    positionsWithinRange.add(positionDTO)
                }
            }

            Log.d(TAG, "$positionsWithinRange")
            if (positionsWithinRange.isNotEmpty()) {
                val description = StringBuilder()
                positionsWithinRange.forEach {
                    with(description) {
                        appendLine("${it.position.title} - ${it.distanceTo}")
                    }
                }
                positionsWithinRange

                val startActivityIntent = MapFragment.newIntent(this@LocationService)
                val pendingIntent = PendingIntent.getActivity(this@LocationService, MAP_FRAGMENT_PENDING_INTENT_REQUEST_CODE, startActivityIntent, 0)
                val notification = createNotification("Location Service", description.toString(), pendingIntent)
                startForeground(NOTIFICATION_REQUEST_CODE, notification)
            } else {
                val startActivityIntent = MapFragment.newIntent(this@LocationService)
                val pendingIntent = PendingIntent.getActivity(this@LocationService, MAP_FRAGMENT_PENDING_INTENT_REQUEST_CODE, startActivityIntent, 0)
                val notification = createNotification("Location Service", "not within range rn.." ,pendingIntent)
                startForeground(NOTIFICATION_REQUEST_CODE, notification)

            }

        }

    }

    private fun createNotification(title: String, description: String, pendingIntent: PendingIntent): Notification {
        val notification = NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .build()

        return notification
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val startActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, MAIN_PENDING_INTENT_REQUEST_CODE, startActivityIntent, 0)

        // Lägg strängar i resources
        val notification = createNotification("Location Service", "Tracking location", pendingIntent)
        startForeground(NOTIFICATION_REQUEST_CODE, notification)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = createLocationRequest(50000, 40000)

        if(checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        positionsLiveData.observe(this) {
            positions = it
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun createLocationRequest(interval: Long, fastestInterval: Long, priority: Int = LocationRequest.PRIORITY_HIGH_ACCURACY): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = interval
        locationRequest.fastestInterval = fastestInterval
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

}