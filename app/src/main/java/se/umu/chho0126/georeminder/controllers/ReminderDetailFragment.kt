package se.umu.chho0126.georeminder.controllers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import se.umu.chho0126.georeminder.R
import se.umu.chho0126.georeminder.models.Position
import se.umu.chho0126.georeminder.viewmodels.ReminderDetailViewModel
import java.util.*

private const val TAG = "ReminderDetailFragment"

/**
 * Represents the Reminder editing screen.
 */
class ReminderDetailFragment : Fragment() {
    private lateinit var titleEditText: EditText
    private lateinit var radiusEditText: EditText
    private lateinit var dateField: TextView
    private lateinit var deleteButton: Button
    private lateinit var position: Position

    private val reminderDetailViewModel: ReminderDetailViewModel by lazy {
        ViewModelProvider(this).get(ReminderDetailViewModel::class.java)
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

    /**
     * Initialize views
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reminder, container, false)
        titleEditText = view.findViewById(R.id.reminder_title)
        radiusEditText = view.findViewById(R.id.reminder_radius)
        dateField = view.findViewById(R.id.reminder_date)
        deleteButton = view.findViewById(R.id.reminder_button_delete)
        return view
    }

    /**
     * Observe the [ReminderDetailViewModel.positionLiveData] property and perform operations on
     * updates.
     */
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

    /**
     * Initializes listeners
     */
    override fun onStart() {
        super.onStart()

        deleteButton.setOnClickListener {
            reminderDetailViewModel.deletePosition(position)
            parentFragmentManager.popBackStack()
        }

        val titleWatcher = createWatcher {
            position.title = it.toString()
        }

        val radiusWatcher = createWatcher {
            if (it.isNullOrEmpty()) return@createWatcher
            position.radius = it.toString().toDouble()
        }
        radiusEditText.addTextChangedListener(radiusWatcher)
        titleEditText.addTextChangedListener(titleWatcher)
    }

    // Create a watcher with specified callback.
    private fun createWatcher(callback: (fieldString: CharSequence?) -> Unit) = object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            callback(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun afterTextChanged(s: Editable?) {
        }
    }

    /**
     * Save the current position on onStop lifecycle function.
     */
    override fun onStop() {
        super.onStop()
        reminderDetailViewModel.savePosition(position)
    }

    private fun updateUI() {
        with (position) {
            titleEditText.setText(title)
            radiusEditText.setText(radius.toString())
            dateField.text = date.toString()
        }
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