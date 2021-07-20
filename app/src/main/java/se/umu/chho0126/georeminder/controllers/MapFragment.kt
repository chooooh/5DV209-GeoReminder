package se.umu.chho0126.georeminder.controllers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
private const val ARG_MAP_LAT= "map_lat"
private const val ARG_MAP_LONG= "map_lon"

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel
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
        super.onViewCreated(view, savedInstanceState)
        mapViewModel.mapLiveData.observe(viewLifecycleOwner, { position ->
            position?.let {
                this.position = position
                Log.d(TAG, "mapLiveData: $position")
            }
        })
    }

    override fun onResume() {
        super.onResume()
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
        googleMap.setOnMapClickListener {
            val lat = it.latitude
            val lon = it.longitude
            mapRepository.addPosition(Position(UUID.randomUUID(), "$lat | $lon", lat, lon))
            googleMap.addMarker(MarkerOptions().position(it))
        }
        googleMap.setOnMarkerClickListener(this)
        // TEST CODE ==========================
        if (position == null) {
            return
        }
        val (_, title, lat, long) = position!!
        Log.d(TAG, "$lat, $long")
        val marker = LatLng(lat, long)

        // TEST CODE ==========================
        with(googleMap) {
            addMarker(
                MarkerOptions()
                    .position(marker)
                    .title("Marker in $title")
            )
            moveCamera(CameraUpdateFactory.newLatLng(marker))
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true

        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "${marker.title}")
        return false
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