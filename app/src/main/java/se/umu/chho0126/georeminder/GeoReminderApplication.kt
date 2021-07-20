package se.umu.chho0126.georeminder

import android.app.Application

class GeoReminderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapRepository.initialize(this)
    }
}