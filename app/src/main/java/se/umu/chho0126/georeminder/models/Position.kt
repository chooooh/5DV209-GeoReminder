package se.umu.chho0126.georeminder.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Position(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var latitude: Double,
    var longitude: Double,
    var date: Date = Date()
)