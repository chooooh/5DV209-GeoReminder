package se.umu.chho0126.georeminder.controllers

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import se.umu.chho0126.georeminder.R
import java.util.*

private const val TAG = "ReminderDialogFragment"
private const val ARG_REMINDER = "reminder"
private const val ARG_ID = "reminder_id"

class ReminderDialogFragment : DialogFragment() {
    private lateinit var listener: Callbacks
    private lateinit var reminderEditText: EditText
    interface Callbacks {
        fun onSave(id: UUID, reminder: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as Callbacks
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement ReminderDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_reminder, null)

            val id = arguments?.getSerializable(ARG_ID) as UUID

            reminderEditText = view.findViewById(R.id.reminder_edit_text)
            builder.setView(view)
            builder.setPositiveButton(R.string.dialog_reminder_save) { _, _ ->
                Log.d(TAG, "pressed positive button ${reminderEditText.text}")
                listener.onSave(id, reminderEditText.text.toString())
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