package se.umu.chho0126.georeminder.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import se.umu.chho0126.georeminder.models.Position


@Database(entities = [Position::class], version = 1)
@TypeConverters(PositionTypeConverters::class)
abstract class MapDatabase : RoomDatabase() {
    abstract fun positionDao(): PositionDao
}