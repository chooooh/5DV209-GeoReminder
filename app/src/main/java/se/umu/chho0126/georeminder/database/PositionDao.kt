package se.umu.chho0126.georeminder.database

import androidx.lifecycle.LiveData
import androidx.room.*
import se.umu.chho0126.georeminder.models.Position
import java.util.*

/**
 * DAO defining various SQL queries.
 */
@Dao
interface PositionDao {

    @Query("SELECT * FROM position")
    fun getPositions(): LiveData<List<Position>>

    @Query("SELECT * FROM position WHERE id=(:id)")
    fun getPosition(id: UUID): LiveData<Position>

    @Query("UPDATE position SET title=(:title) WHERE id=(:id)")
    fun updatePositionTitle(id: UUID, title: String)

    @Query("UPDATE position SET radius=(:radius) WHERE id=(:id)")
    fun updatePositionRadius(id: UUID, radius: Double)

    @Query("UPDATE position SET isEnabled=(:isEnabled) WHERE id=(:id)")
    fun updatePositionStatus(id: UUID, isEnabled: Boolean)

    @Update
    fun updatePosition(position: Position)

    @Insert
    fun addPosition(position: Position)

    @Delete
    fun deletePosition(position: Position)

}
