package se.umu.chho0126.georeminder.controllers

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import se.umu.chho0126.georeminder.MapRepository
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.viewmodels.MapListViewModel
import java.util.*
import kotlin.math.round


private const val TAG = "MapListFragment"
class MapListFragment: Fragment() {

    private lateinit var mapRecyclerView: RecyclerView
    private lateinit var button: Button
    private lateinit var mapListViewModel: MapListViewModel

    private var callbacks: Callbacks? = null
    private var mapAdapter = MapAdapter(emptyList())
    private var mapRepository = MapRepository.get()

    interface Callbacks {
        fun onMapSelected(mapId: UUID)
        fun onAddMap()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapListViewModel = ViewModelProvider(requireActivity()).get(MapListViewModel::class.java)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_list, container, false)
        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            callbacks?.onAddMap()
        }

        mapRecyclerView = view.findViewById(R.id.map_recycler_view)
        mapRecyclerView.layoutManager = LinearLayoutManager(context)
        mapRecyclerView.adapter = mapAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapListViewModel.mapListLiveData.observe(
            viewLifecycleOwner,
            {
                Log.i(TAG, "Got positions ${it.size}")
                updateUI(it)
            }
        )
    }

    private fun updateUI(positions: List<Position>) {
        mapAdapter = MapAdapter(positions)
        mapRecyclerView.adapter = mapAdapter
    }

    private inner class MapHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener{
        private lateinit var position: Position
        private val titleTextView: TextView = itemView.findViewById(R.id.map_title)
        private val latTextView: TextView = itemView.findViewById(R.id.text_lat)
        private val longTextView: TextView = itemView.findViewById(R.id.text_long)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.imageButton)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(position: Position) {
            this.position = position
            titleTextView.text = position.title
            latTextView.text = position.latitude.round(2).toString()
            longTextView.text = position.longitude.round(2).toString()

            deleteButton.setOnClickListener {
                Log.d(TAG, "Deleting.")
                mapRepository.deletePosition(position)
            }
        }

        override fun onClick(v: View?) {
            Log.d(TAG, "ViewHolder clicked..")
            callbacks?.onMapSelected(position.id)
        }
    }

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