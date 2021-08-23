package se.umu.chho0126.georeminder.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import se.umu.chho0126.georeminder.database.MapDatabase
import se.umu.chho0126.georeminder.database.migration_1_2
import se.umu.chho0126.georeminder.database.migration_2_3
import se.umu.chho0126.georeminder.models.Position
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "map-database"

/**
 * Singleton Repository that provides access to the database
 */
class MapRepository private constructor(context: Context) {
    private val database : MapDatabase = Room.databaseBuilder(
        context.applicationContext,
        MapDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2).addMigrations(migration_2_3)
        .build()

    private val positionDao = database.positionDao()
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Retrieves positions from database
     * @return [LiveData] wrapping a [List] of [Position]
     */
    fun getPositions(): LiveData<List<Position>> = positionDao.getPositions()

    /**
     * Retrieves position with the specified id from database
     * @param positionId
     * @return [LiveData] wrapping a [Position]
     */
    fun getPosition(positionId: UUID): LiveData<Position> = positionDao.getPosition(positionId)

    /**
     * Inserts specified [Position] in database on a separate thread
     * @param position [Position] to add
     */
    fun addPosition(position: Position) {
        executor.execute {
            positionDao.addPosition(position)
        }
    }

    /**
     * Deletes the specified [Position] in database on a separate thread
     * @param position [Position] to delete
     */
    fun deletePosition(position: Position) {
        executor.execute {
            positionDao.deletePosition(position)
        }
    }

    /**
     * Updates the specified [Position] in database on a separate thread
     * @param position [Position] to update
     */
    fun updatePosition(position: Position) {
        executor.execute {
            positionDao.updatePosition(position)
        }
    }

    /**
     * Updates the [Position.title] with the specified [positionId] in database on a separate thread
     * @param positionId position with specified [UUID] to update
     * @param title the new title
     */
    fun updatePositionTitle(positionId: UUID, title: String) {
        executor.execute {
            positionDao.updatePositionTitle(positionId, title)
        }
    }

    /**
     * Updates the [Position.radius] with the specified [positionId] in database on a separate thread
     * @param positionId position with specified [UUID] to update
     * @param radius the new radius
     */
    fun updatePositionRadius(positionId: UUID, radius: Double) {
        executor.execute {
            positionDao.updatePositionRadius(positionId, radius)
        }
    }

    /**
     * Updates the [Position.isEnabled] with the specified [positionId] in database on a separate thread
     * @param positionId position with specified [UUID] to update
     * @param radius the new radius
     */
    fun updatePositionStatus(id: UUID, isEnabled: Boolean) {
        executor.execute {
            positionDao.updatePositionStatus(id, isEnabled)
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