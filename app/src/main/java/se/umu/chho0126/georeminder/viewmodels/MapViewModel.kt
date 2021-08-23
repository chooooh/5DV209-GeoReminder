package se.umu.chho0126.georeminder.viewmodels

import androidx.lifecycle.*
import com.google.android.gms.maps.model.CameraPosition
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.repository.MapRepository
import java.util.*

private const val TAG = "MapViewModel"
private const val CAMERA_POSITION = "CAMERA_POSITION"

/**
 * ViewModel specifically for the MapFragment.
 * @property positionListLiveData Contains positions (reminder) stores in the application
 * @property positionLiveData Position wrapped in a [LiveData] type, notifies observers when
 * the function loadPosition is invoked.
 */
class MapViewModel(private val state: SavedStateHandle) : ViewModel() {
    private val mapRepository = MapRepository.get()
    private val positionIdLiveData = MutableLiveData<UUID>()
    val positionListLiveData = mapRepository.getPositions()
    var cameraPosition = state.get<CameraPosition>(CAMERA_POSITION) ?: null

    var positionLiveData: LiveData<Position> = Transformations.switchMap(positionIdLiveData) {
        mapRepository.getPosition(it)
    }

    /**
     * Unique ID updates invokes a transformation on the property positionLiveData by retrieving
     * specified [id] from the [MapRepository].
     * @param [id] [Position] with the specified [UUID] to load
     */
    fun loadPosition(id: UUID) {
        positionIdLiveData.value = id
    }

    /**
     * Retrieves specified [id] from [MapRepository]
     * @param [id] [Position] with the specified [UUID] to retrieve
     */
    fun getPosition(id: UUID): LiveData<Position> {
        return mapRepository.getPosition(id)
    }

    /**
     * Save the [cameraPosition] state
     * @param [cameraPosition] to save
     */
    fun saveCameraPositionState(cameraPosition: CameraPosition) {
        state.set(CAMERA_POSITION, cameraPosition)
    }
}