package se.umu.chho0126.georeminder.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import se.umu.chho0126.georeminder.models.Position
import java.util.*

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

    @Insert
    fun addPosition(position: Position)

    @Delete
    fun deletePosition(position: Position)

}
