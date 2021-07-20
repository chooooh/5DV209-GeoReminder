package se.umu.chho0126.georeminder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.models.Position
import java.util.*

class MapViewModel : ViewModel() {
    private val mapRepository = MapRepository.get()
    private val positionIdLiveData = MutableLiveData<UUID>()

    var mapLiveData: LiveData<Position> = Transformations.switchMap(positionIdLiveData) {
        mapRepository.getPosition(it)
    }

    fun loadPosition(id: UUID) {
        positionIdLiveData.value = id
    }

}