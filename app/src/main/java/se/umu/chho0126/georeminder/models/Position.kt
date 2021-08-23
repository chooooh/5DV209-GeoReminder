package se.umu.chho0126.georeminder.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Represents a reminder.
 * @property isEnabled [Boolean] describing whether the reminder is enabled.
 */
// todo: byt namn
@Entity
data class Position(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var date: Date = Date(),
    var radius: Double = 0.0,
    var isEnabled: Boolean = false
) {
    val getLatLng: LatLng
        get() = LatLng(latitude, longitude)
}
