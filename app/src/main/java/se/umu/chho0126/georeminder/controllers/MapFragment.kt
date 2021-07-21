package se.umu.chho0126.georeminder.controllers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResultListener
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
    private lateinit var googleMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    private lateinit var marker: Marker
    private var position: Position? = null
    private val mapRepository = MapRepository.get() // denna ska väl inte vara här?

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
        // ta bort
        setFragmentResultListener(REQUEST_REMINDER) { requestKey, bundle ->
            Log.d(TAG, "In OnFragmentResult")
            when (requestKey) {
                REQUEST_REMINDER -> {
                    Log.d(TAG, "Received result for $requestKey")
                }

            }
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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapViewModel.mapLiveData.observe(viewLifecycleOwner, { position ->
            this.position = position
            Log.d(TAG, "mapLiveData: $position")
        })
        mapView.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        // prova lägg till markers här!
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        with(googleMap) {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true

            setOnMapClickListener(this@MapFragment)
            setOnMarkerClickListener(this@MapFragment)
        }

        position?.let {
            Log.d(TAG, "position is no longer null, now placing marker!")
            placeMarker(it)
        }
    }

    override fun onMapClick(latLng: LatLng) {
        Log.d(TAG, "Clicking")
        val lat = latLng.latitude
        val lon = latLng.longitude
        val positionId = UUID.randomUUID()
        mapRepository.addPosition(Position(positionId, "untitled", lat, lon))
        ReminderDialogFragment.newInstance(positionId).apply {
            show(this@MapFragment.childFragmentManager, REQUEST_REMINDER)
        }
        val markerPosition = MarkerOptions().position(latLng)
        marker = googleMap.addMarker(markerPosition)
    }

    override fun onSave(id: UUID, reminder: String) {
        Log.d(TAG, "OnSave in mapfragment!!!!")
        mapRepository.updatePositionTitle(id, reminder)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "${marker.title}")
        return false
    }

    private fun placeMarker(pos: Position) {
        with(googleMap) {
            val (_, title, lat, long) = pos
            val position = LatLng(lat, long)
            val marker = MarkerOptions().position(position).title(title)
            addMarker(marker)
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_LEVEL_LANDMASS))
        }
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