package se.umu.chho0126.georeminder

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

const val PREF_IS_TRACKING = "isTracking"

/**
 * Singleton class handling [PreferenceManager]
 */
object Preferences {

    /**
     * Retrieves the state of [PREF_IS_TRACKING] from the [PreferenceManager]
     * @param context
     * @return [Boolean] representing the state of [PREF_IS_TRACKING]
     */
    fun isTracking(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_TRACKING, false)
    }

    /**
     * Retrieves the state of [PREF_IS_TRACKING] from the [PreferenceManager]
     * @return [Boolean] representing the state of [PREF_IS_TRACKING]
     */
    fun setTracking(context: Context, isOn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putBoolean(PREF_IS_TRACKING, isOn)
            }

    }
}