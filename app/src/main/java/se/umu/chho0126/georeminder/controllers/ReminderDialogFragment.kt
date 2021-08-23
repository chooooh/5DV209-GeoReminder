package se.umu.chho0126.georeminder.controllers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import se.umu.chho0126.georeminder.R
import java.util.*

private const val TAG = "ReminderDialogFragment"
private const val ARG_ID = "reminder_id"

/**
 * Represents a dialog prompting user for the title and radius of the Reminder.
 */
class ReminderDialogFragment : DialogFragment() {
    private lateinit var reminderEditText: EditText
    private lateinit var reminderEditRadius: EditText
    private var callbacks: Callbacks? = null

    interface Callbacks {
        /**
         * This function is invoked when the save button is pressed.
         */
        fun onSave(id: UUID, title: String, radius: Double)
    }

    /**
     * Cast context as the callback interface, requiring the context to implement the interface
     * function.
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callbacks = parentFragment as Callbacks
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ReminderDialogListener")
        }
    }

    /**
     * Remove callbacks when detaching this fragment.
     */
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    /**
     * Constructs the dialog view and setups the button listener.
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
                val radius = if (reminderEditRadius.text.isEmpty()) {
                   0.0
                } else {
                    reminderEditRadius.text.toString().toDouble()
                }
                callbacks?.onSave(id, reminderEditText.text.toString(), radius)
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