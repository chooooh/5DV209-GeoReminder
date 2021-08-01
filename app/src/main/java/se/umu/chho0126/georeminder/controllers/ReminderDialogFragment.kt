package se.umu.chho0126.georeminder.controllers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import se.umu.chho0126.georeminder.R
import java.util.*

private const val TAG = "ReminderDialogFragment"
private const val ARG_REMINDER = "reminder"
private const val ARG_ID = "reminder_id"

/**
 * Represents a dialog prompting for the title of the marker.
 */
class ReminderDialogFragment : DialogFragment() {
    private lateinit var reminderEditText: EditText
    private lateinit var reminderEditRadius: EditText
    private var callbacks: Callbacks? = null

    interface Callbacks {
        /**
         * This function is invoked when the save button is pressed.
         */
        fun onSave(id: UUID, reminder: String, radius: Double)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callbacks = parentFragment as Callbacks
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement ReminderDialogListener"))
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    /**
     * Constructs the dialog view and setup the button listener.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_reminder, null)

            val id = arguments?.getSerializable(ARG_ID) as UUID

            reminderEditText = view.findViewById(R.id.dialog_reminder_edit_text)
            reminderEditRadius = view.findViewById(R.id.dialog_reminder_edit_radius)
            builder.setView(view)
            builder.setPositiveButton(R.string.dialog_reminder_save) { _, _ ->
                Log.d(TAG, "pressed positive button ${reminderEditText.text}")
                callbacks?.onSave(id, reminderEditText.text.toString(), reminderEditRadius.text.toString().toDouble())
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        fun newInstance(id: UUID): ReminderDialogFragment {
            val args = Bundle().apply {
                putSerializable(ARG_ID, id)
            }
            return ReminderDialogFragment().apply {
                arguments = args
            }
        }
    }

}