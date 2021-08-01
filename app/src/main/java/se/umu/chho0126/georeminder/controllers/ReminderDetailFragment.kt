package se.umu.chho0126.georeminder.controllers

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.viewmodels.ReminderDetailViewModel
import java.util.*
private const val TAG = "ReminderDetailFragment"

class ReminderDetailFragment : Fragment() {
    private lateinit var titleEditText: EditText
    private lateinit var radiusEditText: EditText
    private lateinit var dateField: TextView
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var mapButton: Button
    private lateinit var toggleSwitch: Switch
    private lateinit var position: Position

    private var callback: Callbacks? = null

    private val reminderDetailViewModel: ReminderDetailViewModel by lazy {
        ViewModelProvider(this).get(ReminderDetailViewModel::class.java)
    }

    interface Callbacks {
        fun onMapButtonClicked(id: UUID)
    }

    /**
     * Initialize corresponding ViewModel and retrieve the ID passed in the bundle arguments.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = Position()
        val positionId: UUID = arguments?.getSerializable(ARG_REMINDER_ID) as UUID
        reminderDetailViewModel.loadPosition(positionId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reminder, container, false)
        titleEditText = view.findViewById(R.id.reminder_title)
        radiusEditText = view.findViewById(R.id.reminder_radius)
        dateField = view.findViewById(R.id.reminder_date)
        saveButton = view.findViewById(R.id.reminder_button_save)
        deleteButton = view.findViewById(R.id.reminder_button_delete)
        toggleSwitch = view.findViewById(R.id.reminder_toggle)
        mapButton = view.findViewById(R.id.reminder_button_map)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reminderDetailViewModel.positionLiveData.observe(
            viewLifecycleOwner,
            {
                it?.let {
                    this.position = it
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        mapButton.setOnClickListener {
            callback?.onMapButtonClicked(position.id)
        }

        saveButton.setOnClickListener {
            reminderDetailViewModel.updatePosition(position.id, titleEditText.text.toString(), radiusEditText.text.toString().toDouble())
        }

        deleteButton.setOnClickListener {
            reminderDetailViewModel.deletePosition(position)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as Callbacks?
    }

    private fun updateUI() {
        titleEditText.setText(position.title)
        radiusEditText.setText(position.radius.toString())
        dateField.text = position.date.toString()
        //toggleSwitch.isChecked = position.isEnabled
    }

    /**
     * Provides helper functions to create this Fragment.
     */
    companion object {
        const val ARG_REMINDER_ID = "reminder_id"
        fun newInstance(id: UUID): ReminderDetailFragment {
            val args = Bundle().apply {
                putSerializable(ARG_REMINDER_ID, id)
            }

            return ReminderDetailFragment().apply {
                arguments = args
            }
        }
    }
}