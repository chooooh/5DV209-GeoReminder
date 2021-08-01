package se.umu.chho0126.georeminder.controllers

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import se.umu.chho0126.georeminder.R
import java.util.*
private const val TAG = "MainActivity"

/**
 * The main entry point of the application. This activity is responsible for starting and displaying
 * various fragments. The activity also enables and disbles the location service.
 */
class MainActivity : AppCompatActivity(), MapListFragment.Callbacks, ReminderDetailFragment.Callbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = MapListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationService::class.java)
        stopService(intent)
    }

    override fun onMapSelected(positionId: UUID) {
        val fragment = ReminderDetailFragment.newInstance(positionId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Overriding MapListFragment's callback function. This function starts the map.
     */
    override fun onAddMap() {
        val fragment = MapFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(android.R.anim.slide_in_left, 0, android.R.anim.slide_in_left, 0)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onMapButtonClicked(id: UUID) {
        val fragment = MapFragment.newInstance(id)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Overriding MapListFragment's callback function. This function enables the location service.
     */
    override fun onEnable() {
        startLocationService()
    }

    /**
     * Overriding MapListFragment's callback function. This function disables the location service.
     */
    override fun onDisable() {
        stopLocationService()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }


}