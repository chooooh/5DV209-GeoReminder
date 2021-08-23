package se.umu.chho0126.georeminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.repository.MapRepository
import java.util.*

/**
 * ViewModel specifically for MapFragment
 * @property positionListLiveData A [LiveData] object wrapping a [List] of [Position]
 */
class MapListViewModel : ViewModel() {

    private val mapRepository = MapRepository.get()
    val positionListLiveData = mapRepository.getPositions()

    /**
     * Inserts specified [position] in database.
     * @param position [Position] to insert
     */
    fun addPosition(position: Position) {
        mapRepository.addPosition(position)
    }

    /**
     * Updates [Position.isEnabled] with specified [id] in database.
     * @param id [Position] with the specified [id] to update
     * @param isEnabled [Boolean] [Position.isEnabled] to set
     */
    fun updatePositionStatus(id: UUID, isEnabled: Boolean) {
        mapRepository.updatePositionStatus(id, isEnabled)
    }
}