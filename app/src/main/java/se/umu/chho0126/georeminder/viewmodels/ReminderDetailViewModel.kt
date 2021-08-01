package se.umu.chho0126.georeminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.models.Position
import java.util.*

class ReminderDetailViewModel : ViewModel() {
    private val mapRepository = MapRepository.get()
    private val positionIdLiveData = MutableLiveData<UUID>()

    var positionLiveData: LiveData<Position> = Transformations.switchMap(positionIdLiveData) {
        mapRepository.getPosition(it)
    }

    fun loadPosition(id: UUID) {
        positionIdLiveData.value = id
    }

    fun updatePosition(id: UUID, title: String, radius: Double) {
        mapRepository.updatePositionTitle(id, title)
        mapRepository.updatePositionRadius(id, radius)
    }

    fun deletePosition(position: Position) {
        mapRepository.deletePosition(position)
    }

}
