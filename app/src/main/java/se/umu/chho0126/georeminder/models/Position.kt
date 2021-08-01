package se.umu.chho0126.georeminder.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.format.DateTimeFormatter
import java.util.*

@Entity
data class Position(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var date: Date = Date(),
    var radius: Double = 0.0
)
    /*
    var radius: Double = 0.0
        set(value) {
            if (value < 10.0) throw IllegalArgumentException("Radius cannot be less than ")
        }

     */
