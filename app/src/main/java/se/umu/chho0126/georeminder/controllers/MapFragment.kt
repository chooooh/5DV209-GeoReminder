package se.umu.chho0126.georeminder.controllers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.viewmodels.MapViewModel
import java.util.*


private const val TAG = "MapFragment"
private const val REQUEST_REMINDER = "DialogReminder"

private const val ZOOM_LEVEL_LANDMASS = 5f
private const val ZOOM_LEVEL_CITY = 10f
private const val ZOOM_LEVEL_STREET = 15f

/**
 * This Fragment represents the map screen.
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ReminderDialogFragment.Callbacks {
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var receivedLocation: Boolean = false
    private var positions: List<Position> = listOf()
    private val mapRepository = MapRepository.get() // denna ska väl inte vara här?
    private var marker: Marker? = null
    private var googleMap: GoogleMap? = null

    /**
     * Initialises the corresponding ViewModel. Also loads position if a position ID were
     * specified as argument.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        val positionId: UUID? = arguments?.getSerializable(ARG_POSITION_ID) as? UUID
        if (positionId != null) {
            receivedLocation = true
            mapViewModel.loadPosition(positionId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        return view
    }

    /**
     * Initialize the Google mapView. This function also begins observing position data, placing
     * and focusing on markers on position data changes.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        mapViewModel.positionListLiveData.observe(viewLifecycleOwner) {
            positions = it
            it?.let {
                googleMap?.clear()
                placeCircles(it)
                placeMarkers(it)
            }
        }
        mapViewModel.positionLiveData.observe(viewLifecycleOwner) { position ->
            position?.let {
                focusCameraAt(it, ZOOM_LEVEL_STREET)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // prova lägg till markers här!
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        Log.d(TAG, "onPause: ${googleMap?.cameraPosition.toString()}")
        googleMap?.let {
            mapViewModel.saveCameraPositionState(it.cameraPosition)
        }
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    // Initial camera setup. Focuses camera at the device's current location is permission for
    // location tracking is acquired.
    private fun initialCameraSetup(googleMap: GoogleMap) {
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (permission && mapViewModel.cameraPosition == null) {
            googleMap.isMyLocationEnabled = true
            if (!receivedLocation) {
                googleMap.uiSettings.isMyLocationButtonEnabled = true
                fusedLocationProviderClient = FusedLocationProviderClient(requireContext())
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    it?.let {
                        focusCameraAt(it, ZOOM_LEVEL_STREET)
                    }
                }
            }
        }
    }


    /**
     * Places all positions retrieved from the repository once the map is ready. Also initializes
     * listeners.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        with(googleMap) {
            uiSettings.isZoomControlsEnabled = true

            Log.d(TAG, "onMapReady")
            initialCameraSetup(this)


            setOnMapClickListener(this@MapFragment)
            setOnMarkerClickListener(this@MapFragment)
        }

        positions.let {
            placeCircles(it)
            placeMarkers(it)
        }
    }

    /**
     * Creates a position object on the clicked area and adds it to the database.
     * A DialogFragment also gets instantiated and allows the user to set the title.
     * A marker is then added to the map.
     */
    override fun onMapClick(latLng: LatLng) {
        val lat = latLng.latitude
        val lon = latLng.longitude
        val positionId = UUID.randomUUID()
        mapRepository.addPosition(Position(positionId, "untitled", lat, lon))
        mapViewModel.loadPosition(positionId)
        ReminderDialogFragment.newInstance(positionId).apply {
            show(this@MapFragment.childFragmentManager, REQUEST_REMINDER)
        }


        marker = googleMap?.addMarker(MarkerOptions().position(latLng))
    }

    /**
     * This callback function is eventually called by ReminderDialogFragment.
     * Updates the title for specified position both in the database and in UI.
     * @param id the UUID representing the specified position
     * @param title the String representing the title for the specified position
     */
    override fun onSave(id: UUID, title: String, radius: Double) {
        with (mapRepository) {
            updatePositionTitle(id, title)
            updatePositionRadius(id, radius)
        }
    }

    private fun placeMarker(pos: Position) {
        googleMap?.let {
            val title = pos.title
            val latLng = pos.getLatLng
            val marker = MarkerOptions().position(latLng).title(title)
            it.addMarker(marker)
        }
    }

    private fun placeMarkers(positions: List<Position>) {
        positions.forEach {
            placeMarker(it)
        }
    }

    // Creates a circle around specified position to display the radius
    private fun placeCircle(pos: Position) {
        val (_, _, lat, long) = pos
        val latLng = LatLng(lat, long)
        val circleOptions = CircleOptions()
        val radius: Double = if (pos.radius < 0.0) 0.0 else pos.radius
        with (circleOptions) {
            center(latLng)
            radius(radius)
            strokeColor(Color.BLACK)
            circleOptions.fillColor(0x30ff0000)
            strokeWidth(2.0F)
        }
        googleMap?.addCircle(circleOptions)
    }

    private fun placeCircles(positions: List<Position>) {
        positions.forEach {
            placeCircle(it)
        }
    }

    // Move camera to specified position with the specified zoomLevel
    private fun focusCameraAt(pos: Position, zoomLevel: Float) {
        val latLng = pos.getLatLng
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
    }

    private fun focusCameraAt(loc: Location, zoomLevel: Float) {
        val position = LatLng(loc.latitude, loc.longitude)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))
    }


    /**
     * Provides helper functions to create new instances and encapsulates relevant information.
     */
    companion object {
        const val ARG_POSITION_ID = "map_id"
        fun newInstance(): MapFragment {
            return MapFragment()
        }

        fun newInstance(positionId: UUID): MapFragment {
            val args = Bundle().apply {
                putSerializable(ARG_POSITION_ID, positionId)
            }
            return MapFragment().apply {
                arguments = args
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

}