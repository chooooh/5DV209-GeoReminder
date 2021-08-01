package se.umu.chho0126.georeminder.controllers

import android.Manifest
import android.R
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
import se.umu.chho0126.georeminder.Preferences
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

        private fun createNotificationDescription(): StringBuilder {
            val description = StringBuilder()
            positionsWithinRange.forEach {
                with(description) {
                    appendLine(it.toString())
                }
            }
            return description
        }

        private fun addPositionsWithinRange(currentLocation: Location) {
            positions.forEach {
                val location = Location("")
                location.latitude = it.latitude
                location.longitude = it.longitude
                val distance = currentLocation.distanceTo(location)
                val range = it.radius
                if (distance <= range) {
                    val positionDTO = PositionDTO(it, distance, currentLocation)
                    positionsWithinRange.add(positionDTO)
                }
            }
        }

        private fun notifyUserAboutPositionsWithinRange() {
            if (positionsWithinRange.isNotEmpty()) {
                val description = createNotificationDescription()
                val startActivityIntent = MainActivity.newIntent(this@LocationService)
                startForegroundService(
                    startActivityIntent = startActivityIntent,
                    context = this@LocationService,
                    pendRequestCode = MAIN_PENDING_INTENT_REQUEST_CODE,
                    notRequestCode = NOTIFICATION_REQUEST_CODE,
                    notTitle = "Location Service",
                    notDesc = description.toString()
                )
            }
        }

        override fun onLocationResult(locationResult: LocationResult) {
            val currentLocation = locationResult.lastLocation
            positionsWithinRange = mutableListOf()

            addPositionsWithinRange(currentLocation)
            notifyUserAboutPositionsWithinRange()
        }
    }

    private fun startForegroundService(
        startActivityIntent: Intent,
        context: Context,
        pendRequestCode: Int,
        notRequestCode: Int,
        notTitle: String,
        notDesc: String
    ) {
        val pendingIntent = PendingIntent.getActivity(context, pendRequestCode, startActivityIntent, 0)
        val notification = createNotification(notTitle, notDesc, pendingIntent)
        startForeground(notRequestCode, notification)
    }

    private fun createNotification(
        title: String,
        description: String,
        pendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_menu_report_image)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "onStartCommand")

        val startActivityIntent = MainActivity.newIntent(this)
        startForegroundService(
            startActivityIntent = startActivityIntent,
            context = this,
            pendRequestCode = MAIN_PENDING_INTENT_REQUEST_CODE,
            notRequestCode = NOTIFICATION_REQUEST_CODE,
            notTitle = "Location Service",
            notDesc = "Tracking location"
        )

        if(checkLocationPermission()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            //val locationRequest = createLocationRequest(120000, 120000)
            val locationRequest = createLocationRequest(12000, 12000)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        positionsLiveData.observe(this) {
            positions = it
        }

        return START_STICKY
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.d(TAG, "onLowMemory")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d(TAG, "onTrimMemory")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        Preferences.setTracking(this, false)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun createLocationRequest(interval: Long, fastestInterval: Long, priority: Int = LocationRequest.PRIORITY_HIGH_ACCURACY): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = interval
        locationRequest.fastestInterval = fastestInterval
        locationRequest.priority = priority
        return locationRequest
    }

}