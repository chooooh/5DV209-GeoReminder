package se.umu.chho0126.georeminder.controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import se.umu.chho0126.georeminder.R
import java.util.*

class MainActivity : AppCompatActivity(), MapListFragment.Callbacks {
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

        /*
        if (currentFragment == null) {
            val fragment = MapFragment.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()
        }
         */
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

}