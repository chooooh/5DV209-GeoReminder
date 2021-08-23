package se.umu.chho0126.georeminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import se.umu.chho0126.georeminder.repository.MapRepository

const val NOTIFICATION_CHANNEL_ID = "location_service_channel"

/**
 * Used for initialization of global states, such as MapRepository and also the notification channel.
 */
class GeoReminderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MapRepository.initialize(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            val manager = getSystemService(NotificationManager::class.java)
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)
        }
    }
}