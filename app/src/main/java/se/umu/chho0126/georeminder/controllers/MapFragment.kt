package se.umu.chho0126.georeminder.controllers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.viewmodels.MapViewModel
import java.util.*


private const val TAG = "MapFragment"
private const val ARG_POSITION_ID = "map_id"
private const val ARG_MAP_LAT = "map_lat"
private const val ARG_MAP_LONG = "map_lon"
private const val REQUEST_REMINDER = "DialogReminder"

private const val ZOOM_LEVEL_LANDMASS = 5f
private const val ZOOM_LEVEL_CITY = 10f
private const val ZOOM_LEVEL_STREET = 15f

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ReminderDialogFragment.Callbacks {
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel
    private val mapRepository = MapRepository.get() // denna ska väl inte vara här?
    private var marker: Marker? = null
    private var googleMap: GoogleMap? = null
    private val positionId: UUID? = null
    private var position: Position? = null

    interface Callbacks {
        fun onMarkerAdded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        val positionId: UUID? = arguments?.getSerializable(ARG_POSITION_ID) as? UUID
        Log.d(TAG, "onCreate: positionId: $positionId")
        if (positionId != null) {
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
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        // Sätt in positionListLiveData istället...
        mapViewModel.positionLiveData.observe(viewLifecycleOwner, { position ->
            this.position = position
            /*
            marker?.let {
                val (_, title, lat, lon) = position
                it.position = LatLng(lat, lon)
                it.title = title
            }
             */
            position?.let {
                placeMarker(it)
                focusCameraAt(it, ZOOM_LEVEL_LANDMASS)
                Log.d(TAG, "mapLiveData: $it")
            }
        })
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        // prova lägg till markers här!
        mapView.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        this.googleMap = googleMap
        with(googleMap) {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true

            setOnMapClickListener(this@MapFragment)
            setOnMarkerClickListener(this@MapFragment)
        }

        /*
        position?.let {
            placeMarker(it)
        }

         */
    }

    // Undersök denna...
    override fun onMapClick(latLng: LatLng) {
        Log.d(TAG, "Clicking")
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

    override fun onSave(id: UUID, reminder: String) {
        Log.d(TAG, "OnSave in mapfragment!!!!")
        mapRepository.updatePositionTitle(id, reminder)
        marker?.title = reminder
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "${marker.title}")
        return false
    }

    private fun placeMarker(pos: Position) {
        googleMap?.let {
            val (_, title, lat, long) = pos
            val position = LatLng(lat, long)
            val marker = MarkerOptions().position(position).title(title)
            it.addMarker(marker)
        }

    }

    private fun focusCameraAt(pos: Position, zoomLevel: Float) {
        val (_, _, lat, long) = pos
        val position = LatLng(lat, long)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))
    }

    private fun placeMarkers() {

    }

    companion object {
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



}