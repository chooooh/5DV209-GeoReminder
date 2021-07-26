package se.umu.chho0126.georeminder

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

const val PREF_IS_TRACKING = "isTracking"
object Preferences {
    fun isTracking(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_TRACKING, false)
    }

    fun setTracking(context: Context, isOn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(PREF_IS_TRACKING, isOn)
            }

    }
}