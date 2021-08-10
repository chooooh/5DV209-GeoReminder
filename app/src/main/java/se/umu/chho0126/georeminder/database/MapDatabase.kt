package se.umu.chho0126.georeminder.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.umu.chho0126.georeminder.models.Position

/**
 * Defines the Database. Contains various migrations, primarily to create new columns.
 */
@Database(entities = [Position::class], version = 3)
@TypeConverters(PositionTypeConverters::class)
abstract class MapDatabase : RoomDatabase() {
    abstract fun positionDao(): PositionDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Position ADD COLUMN radius DOUBLE NOT NULL DEFAULT '0.0'")

    }
}

val migration_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Position ADD COLUMN isEnabled INT NOT NULL DEFAULT '0'")
    }
}
