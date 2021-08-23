package se.umu.chho0126.georeminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.repository.MapRepository
import java.util.*

/**
 * ViewModel specifically for ReminderDetailFragment
 * @property positionLiveData Position wrapped in a [LiveData] type. Notifies observers when
 * the function [loadPosition] is invoked
 */
class ReminderDetailViewModel : ViewModel() {
    private val mapRepository = MapRepository.get()
    private val positionIdLiveData = MutableLiveData<UUID>()

    var positionLiveData: LiveData<Position> = Transformations.switchMap(positionIdLiveData) {
        mapRepository.getPosition(it)
    }

    /**
     * Unique ID updates invokes a transformation on the property positionLiveData by retrieving
     * specified [id] from the [MapRepository].
     *
     */
    fun loadPosition(id: UUID) {
        positionIdLiveData.value = id
    }

    /**
     * Saves [position] in database.
     * @param position to save
     */
    fun savePosition(position: Position) {
        mapRepository.updatePosition(position)
    }

    /**
     * Deletes [position] in database.
     * @param position to delete
     */
    fun deletePosition(position: Position) {
        mapRepository.deletePosition(position)
    }

}
