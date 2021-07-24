package se.umu.chho0126.georeminder.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import se.umu.chho0126.georeminder.R
import java.util.*
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), MapListFragment.Callbacks, ReminderDialogFragment.Callbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Lyssna efter location


        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment == null) {
            val fragment = MapListFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }

    }

    override fun onMapSelected(positionId: UUID) {
        val fragment = MapFragment.newInstance(positionId)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onAddMap() {
        val fragment = MapFragment.newInstance()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onSave(id: UUID, reminder: String) {
        Log.d(TAG, "ogoa booga")
    }

}