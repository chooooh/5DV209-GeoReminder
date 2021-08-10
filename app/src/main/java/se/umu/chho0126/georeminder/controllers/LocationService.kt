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
import se.umu.chho0126.georeminder.Preferences
import se.umu.chho0126.georeminder.models.Position
import java.util.*

private const val TAG = "LocationService"
private const val NOTIFICATION_REQUEST_CODE = 1
private const val MAIN_PENDING_INTENT_REQUEST_CODE = 0

// intervals for the location update
private const val INTERVAL: Long = 120000
private const val INTERVAL_FASTEST: Long = 120000

/**
 * Service that tracks location on an interval. Maintains a foreground notification.
 * @property fusedLocationClient Client that provides location updates.
 * @property locationCallback Callback that gets called on every update.
 * @property mapRepository Contains data access functions.
 * @property positionsLiveData Required in order to handle LiveData acquired by repository functions,
 * @property positions List containing all positions (reminders)
 */
class LocationService : LifecycleService(){
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback = MyLocationCallback()
    private var mapRepository: MapRepository = MapRepository.get()
    private var positionsLiveData: LiveData<List<Position>> = mapRepository.getPositions()
    private var positions: List<Position> = listOf()

    private data class PositionDTO(val position: Position, val distanceTo: Float, val userLocation: Location)
    // Callback that gets called on every location update
    private inner class MyLocationCallback: LocationCallback() {
        private var positionsWithinRange: MutableList<PositionDTO> = mutableListOf()

        private fun createNotificationDescription(): StringBuilder {
            val description = StringBuilder()
            for (i in positionsWithinRange.indices) {
                val position = positionsWithinRange[i].position
                if (i == positionsWithinRange.size-1) {
                    description.append(position.title)
                    continue
                }
                description.appendLine(position.title).appendLine()
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
                if (distance <= range && it.isEnabled) {
                    val positionDTO = PositionDTO(it, distance, currentLocation)
                    positionsWithinRange.add(positionDTO)
                }
            }
        }

        private fun notifyUserAboutPositionsWithinRange() {
            if (positionsWithinRange.isNotEmpty()) {
                val description = createNotificationDescription()
                Log.d(TAG, description.toString())
                val startActivityIntent = MainActivity.newIntent(this@LocationService)
                startForegroundService(
                    startActivityIntent = startActivityIntent,
                    context = this@LocationService,
                    pendRequestCode = MAIN_PENDING_INTENT_REQUEST_CODE,
                    notRequestCode = NOTIFICATION_REQUEST_CODE,
                    notTitle = "Reminders within range",
                    notDesc = description.toString()
                )
            }
        }

        // The function that gets called on location updates.
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
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .build()
    }

    /**
     * Initializes a foreground notification and requests relevant permissions.
     * If permissions are acquired, create a location request that enables tracking in
     * specified interval
     */
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
            val locationRequest = createLocationRequest(INTERVAL, INTERVAL_FASTEST)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        positionsLiveData.observe(this) {
            positions = it
        }

        return START_STICKY
    }

    /**
     * Store location tracking flag and remove location updates
     */
    override fun onDestroy() {
        super.onDestroy()
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