package se.umu.chho0126.georeminder.controllers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import se.umu.chho0126.georeminder.Preferences
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.viewmodels.MapListViewModel
import java.util.*
import kotlin.math.round

private const val TAG = "MapListFragment"

/**
 * This Fragment represents the screen with a RecyclerView containing positions.
 */
class MapListFragment: Fragment() {
    private lateinit var mapRecyclerView: RecyclerView
    private lateinit var button: Button
    private lateinit var mapListViewModel: MapListViewModel

    private var callbacks: Callbacks? = null
    private var mapAdapter = MapAdapter(emptyList())

    /**
     * Callback functions that MainActivity handles
     */
    interface Callbacks {
        /**
         * Implementation of this function determines what occurs when the user selects a reminder
         * @param reminderId UUID that represents the selected Reminder
         */
        fun onReminderClicked(reminderId: UUID)

        /**
         * Implementation of this function determines what occurs when the user selects a reminder's
         * map button.
         * @param mapId UUID that represents the selected Reminder
         */
        fun onReminderMapIconClicked(mapId: UUID)

        /**
         * Occurs when the user presses the "Map" button. This is supposed to start the map fragment
         */
        fun onMap()

        /**
         * Occurs when the user presses the "Start Tracking" button. This is supposed to start the
         * location service.
         */
        fun onEnable()

        /**
         * Occurs when the user presses the "Stop Tracking" button. This is supposed to stop the
         * location service.
         */
        fun onDisable()
    }

    //Checks if various permissions were granted. Displays messages accordingly.
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission acquired. Recommend disabling battery saver mode.
            val alertDialog = AlertDialog.Builder(requireContext())
            with (alertDialog) {
                setTitle(getString(R.string.location_permission_battery_saver_title))
                setMessage(getString(R.string.location_permission_battery_saver_message))
                create().show()
            }
        } else {
            // No permission acquired
            val alertDialog = AlertDialog.Builder(requireContext())
            with (alertDialog) {
                setTitle(R.string.location_permission_denied_title)
                setMessage(R.string.location_permission_denied_message)
                create().show()
            }

        }
    }

    // Checks if user has required permissions. Displays a rationale if needed, explaining why
    // permissions are required.
    private fun checkLocationPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        return when {
            permission == PackageManager.PERMISSION_GRANTED -> {
                return true
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                val alertDialog = AlertDialog.Builder(requireContext())
                with (alertDialog) {
                    setTitle(getString(R.string.location_permission_edu_title))
                    setMessage(getString(R.string.location_permission_edu_message))
                    setNegativeButton(getString(R.string.location_permission_edu_cancel_button)) { _, _ ->
                    }
                    create().show()
                }
                return false
            }
            else -> {
                return false
            }
        }
    }

    /**
     * Initialize the corresponding ViewModel and enable the Menu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapListViewModel = ViewModelProvider(requireActivity()).get(MapListViewModel::class.java)
        setHasOptionsMenu(true)
    }

    /**
     * Casts context as [Callbacks].
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    /**
     * Creates the action bar menu. Displays the menu item based on current state of corresponding item.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_map_list, menu)

        val toggleItem = menu.findItem(R.id.menu_item_toggle_location_service)
        val isTracking = Preferences.isTracking(requireContext())
        val toggleItemTitle = if (isTracking) {
            R.string.stop_tracking
        } else {
            R.string.start_tracking
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    /**
     * Defines actions to perform when menu items is pressed. Also verifies that relevant
     * permissions are granted, otherwise launch permission request.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_toggle_location_service -> {
                val isTracking = Preferences.isTracking(requireContext())
                if (isTracking) {
                    Log.d(TAG, "disabling..")
                    callbacks?.onDisable()
                    Preferences.setTracking(requireContext(), false)
                } else {
                    if (!checkLocationPermission()) {
                        // Checka denna?
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        //requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        return true
                    }
                    Log.d(TAG, "enabling..")
                    callbacks?.onEnable()
                    Preferences.setTracking(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Initializes views
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reminder_list, container, false)
        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            callbacks?.onMap()
        }

        mapRecyclerView = view.findViewById(R.id.map_recycler_view)
        mapRecyclerView.layoutManager = LinearLayoutManager(context)
        mapRecyclerView.adapter = mapAdapter

        return view
    }

    /**
     * Begin observing the list livedata. Check if the user has location permission.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapListViewModel.positionListLiveData.observe(
            viewLifecycleOwner,
            {
                Log.i(TAG, "Got positions ${it.size}")
                updateUI(it)
            }
        )
        if (!checkLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(positions: List<Position>) {
        mapAdapter = MapAdapter(positions)
        mapRecyclerView.adapter = mapAdapter
    }

    private inner class MapHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var position: Position
        private val titleTextView: TextView = itemView.findViewById(R.id.map_title)
        private val radiusTextView: TextView = itemView.findViewById(R.id.text_radius)
        private val toggleSwitch: SwitchCompat = itemView.findViewById(R.id.reminder_switch)
        private val mapButton: ImageButton = itemView.findViewById(R.id.reminder_map)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(position: Position) {
            this.position = position
            with (position) {
                titleTextView.text = title
                radiusTextView.text = getString(R.string.meter_label, radius.toInt())
                toggleSwitch.isChecked = isEnabled
            }
            mapButton.setOnClickListener {
                callbacks?.onReminderMapIconClicked(position.id)
            }

            toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
                mapListViewModel.updatePositionStatus(position.id, isChecked)
                mapAdapter.notifyItemChanged(0)
            }
        }

        override fun onClick(v: View?) {
            callbacks?.onReminderClicked(position.id)
        }
    }

    /**
     * Round the decimal to a specified number of integers
     * @param decimals The number of decimals to round to
     */
    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) {
            multiplier *= 10
        }
        return round(this * multiplier) / multiplier
    }

    private inner class MapAdapter(var maps: List<Position>): RecyclerView.Adapter<MapHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapHolder {
            val view = layoutInflater.inflate(R.layout.list_item_map, parent, false)
            return MapHolder(view)
        }

        override fun onBindViewHolder(holder: MapHolder, position: Int) {
            val exercise = maps[position]
            holder.bind(exercise)
        }

        override fun getItemCount(): Int {
            return maps.size
        }
    }

    companion object {
        fun newInstance(): MapListFragment {
            return MapListFragment()
        }
    }
}