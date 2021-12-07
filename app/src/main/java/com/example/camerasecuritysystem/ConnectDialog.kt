package com.example.camerasecuritysystem

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment

class ConnectDialog : AppCompatDialogFragment() {

    private var listener: ConnectDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.connect_dialog_layout, null)

        val editTextUsername = view.findViewById<EditText>(R.id.edit_camera_id)
        val editTextPassword = view.findViewById<EditText>(R.id.edit_password)

        return AlertDialog.Builder(requireActivity())
            .setView(view)
            .setTitle("Login")
            .setNegativeButton("Cancel") {_, _, ->}
            .setPositiveButton("Ok") {_, _, ->
                val username = editTextUsername.text.toString()
                val password = editTextPassword.text.toString()
                listener!!.applyTexts(username, password)
            }
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as ConnectDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "must implement ExampleDialogListener"
            )
        }
    }

    interface ConnectDialogListener {
        fun applyTexts(username: String?, password: String?)
    }
}