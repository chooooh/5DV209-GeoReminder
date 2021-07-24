package se.umu.chho0126.georeminder.controllers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
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
private const val REQUEST_REMINDER = "DialogReminder"

private const val ZOOM_LEVEL_LANDMASS = 5f
private const val ZOOM_LEVEL_CITY = 10f
private const val ZOOM_LEVEL_STREET = 15f


class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, ReminderDialogFragment.Callbacks {
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: LocationResult
    private var locationCallback: LocationCallback = MyLocationCallback()
    private var positions: List<Position> = listOf()
    private val mapRepository = MapRepository.get() // denna ska väl inte vara här?
    private var marker: Marker? = null
    private var googleMap: GoogleMap? = null

    private inner class MyLocationCallback: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            googleMap?.clear()
            locationResult.locations.forEach {
                Log.d(TAG, "$it")
                val position = Position(UUID.randomUUID(), "MY LOCATION!", it.latitude, it.longitude)
                placeMarker(position)
            }
            /*
            val position = Position(UUID.randomUUID(), "MY LOCATION!", location.latitude, location.longitude)
            placeMarker(position)
            focusCameraAt(position, ZOOM_LEVEL_LANDMASS)
            positions.forEach {
                val loc = Location("")
                loc.latitude = it.latitude
                loc.longitude = it.longitude
                val distance = location.distanceTo(loc)
                if (distance < 1000) {
                    Toast.makeText(requireContext(), "distance to ${it.title} is $distance", Toast.LENGTH_LONG).show()
                }
                Log.d(TAG, "distance to ${it.title}: $distance")
            }

             */
        }
    }

    val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "isGranted = true")

        } else {
            Log.d(TAG, "isGranted = false")
            //ActivityCompat.requestPermissions(requireActivity(), permissions, MY_PERMISSIONS_REQUEST_LOCATION)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        val positionId: UUID? = arguments?.getSerializable(ARG_POSITION_ID) as? UUID
        if (positionId != null) {
            mapViewModel.loadPosition(positionId)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        mapView.getMapAsync(this)
        // Sätt in positionListLiveData istället...
        mapViewModel.positionListLiveData.observe(viewLifecycleOwner) {
            positions = it
            it?.let {
                Log.d(TAG, "LISTAN GER: ${it.size}")
                googleMap?.clear()
                placeMarkers(it)
            }
        }

        mapViewModel.positionLiveData.observe(viewLifecycleOwner) { position ->
            position?.let {
                focusCameraAt(it, ZOOM_LEVEL_CITY)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
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


        positions?.let {
            placeMarkers(it)
        }
        /*
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Permission: $permission")
        if (permission) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                Log.d(TAG, "true")
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

         */
        if (checkLocationPermission()) {
            val locationRequest = createLocationRequest(10000, 5000)
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
            /*
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                Log.d(TAG, "has permissions.. Performing operations if $location is not null")
                if (location != null) {
                    Log.d(TAG, "$location")
                    val position = Position(UUID.randomUUID(), "MY LOCATION!", location.latitude, location.longitude, )
                    placeMarker(position)
                    focusCameraAt(position, ZOOM_LEVEL_LANDMASS)
                    positions.forEach {
                        val loc = Location("")
                        loc.latitude = it.latitude
                        loc.longitude = it.longitude
                        val distance = location.distanceTo(loc)
                        if (distance < 1000) {
                            Toast.makeText(requireContext(), "distance to ${it.title} is $distance", Toast.LENGTH_LONG).show()
                        }
                        Log.d(TAG, "distance to ${it.title}: $distance")
                    }
                }
            }
             */
        } else {
            Log.d(TAG, "no permission.. Launching permission launcher")
            //ActivityCompat.requestPermissions(requireActivity(), permissions, MY_PERMISSIONS_REQUEST_LOCATION)
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }


    // Undersök denna...
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

    override fun onSave(id: UUID, reminder: String) {
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

    private fun placeMarkers(positions: List<Position>) {
        positions.forEach {
            placeMarker(it)
        }
    }

    private fun checkLocationPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Location permission: $permission")
        return permission
    }

    private fun createLocationRequest(interval: Int, fastestInterval: Int, priority: Int = LocationRequest.PRIORITY_HIGH_ACCURACY): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 851923
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