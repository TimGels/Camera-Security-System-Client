package com.example.camerasecuritysystem

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import java.lang.Exception

class ConnectDialog : AppCompatDialogFragment() {

    private var listener: ConnectDialogListener? = null

    private var keyStore = KeyStoreHelper("connectToServer")

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view: View = inflater.inflate(R.layout.connect_dialog_layout, null)

        //Get the id of the input fields
        val editTextUsername = view.findViewById<EditText>(R.id.edit_camera_id)
        val editTextPort = view.findViewById<EditText>(R.id.edit_port)
        val editTextIPAddress = view.findViewById<EditText>(R.id.edit_ip_address)
        val editTextPassword = view.findViewById<EditText>(R.id.edit_password)

        //Get access to all shared preferences
        sharedPreferences = requireContext().getSharedPreferences(
            "com.example.camerasecuritysystem",
            Context.MODE_PRIVATE)

        //Assign the preferences to variables
        val cameraId = sharedPreferences.getString("camera_id", null)
        val port = sharedPreferences.getString("port", null)
        val ipAddress = sharedPreferences.getString("ip_address", null)

        val encPwd = sharedPreferences.getString("encPwd", null)
        val ivByte = sharedPreferences.getString("pwdIVByte", null)

        //Check if the ivByte and encrypted password exist
        if (ivByte != null && encPwd != null) {

            //Decrypt the IV byte and the password
            try {
                val pwdText = keyStore.decryptData(
                    ivByte.toByteArray(Charsets.ISO_8859_1),
                    encPwd.toByteArray(Charsets.ISO_8859_1)
                )

                //Set the password text in the input field
                editTextPassword.setText(pwdText)

            } catch (e: Exception) {
                Log.e("EXCEPTION", "error: ", e)
            }
        }

        //Set the camera text in the input field
        if (cameraId != null) {
            editTextUsername.setText(cameraId)
        }

        //Set the port text in the input field
        if (port != null) {
            editTextPort.setText(port)
        }

        //Set the IP text in the input field
        if (ipAddress != null) {
            editTextIPAddress.setText(ipAddress)
        }

        //Return the dialog window
        return AlertDialog.Builder(requireActivity())
            .setView(view)
            .setTitle("Login")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Ok") { _, _ ->

                //TODO Hier moet de inputvalidatie komen. Daarnaast moet er een error getoond worden
                // https://stackoverflow.com/questions/30953449/design-android-edittext-to-show-error-message-as-described-by-google
                val cameraIdText = editTextUsername.text.toString()
                val portText = editTextPort.text.toString()
                val ipAddresstext = editTextIPAddress.text.toString()
                val passwordText = editTextPassword.text.toString()

                //Pass the new input values to store them in the shared preferences
                listener!!.applyTexts(cameraIdText, portText, ipAddresstext, passwordText)
            }
            .create()
    }

    /**
     * Check if Settingsactivity implemented ConnectDialogListener
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as ConnectDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "must implement ConnectDialogListener"
            )
        }
    }

    interface ConnectDialogListener {
        fun applyTexts(cameraID: String?, port: String?, ipAddress: String?, password: String?)
    }
}