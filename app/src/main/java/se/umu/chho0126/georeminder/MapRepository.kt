package se.umu.chho0126.georeminder

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import se.umu.chho0126.georeminder.database.MapDatabase
import se.umu.chho0126.georeminder.models.Position
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "map-database"

class MapRepository private constructor(context: Context) {
    private val database : MapDatabase = Room.databaseBuilder(
        context.applicationContext,
        MapDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val positionDao = database.positionDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getPositions(): LiveData<List<Position>> = positionDao.getPositions()
    fun getPosition(positionId: UUID) = positionDao.getPosition(positionId)

    fun addPosition(position: Position) {
        executor.execute {
            positionDao.addPosition(position)
        }
    }

    fun deletePosition(position: Position) {
        executor.execute {
            positionDao.deletePosition(position)
        }
    }


    companion object {
        private var INSTANCE: MapRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MapRepository(context)
            }
        }

        fun get(): MapRepository {
            return INSTANCE ?: throw IllegalStateException("MapRepository must be initialized.")
        }
    }

}