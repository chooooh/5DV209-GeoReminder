package se.umu.chho0126.georeminder.viewmodels

import androidx.lifecycle.ViewModel
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.models.Position

class MapListViewModel : ViewModel() {

    private val mapRepository = MapRepository.get()
    val mapListLiveData = mapRepository.getPositions() // undersök

    fun addPosition(position: Position) {
        mapRepository.addPosition(position)
    }
}