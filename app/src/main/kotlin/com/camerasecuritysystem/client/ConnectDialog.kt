package com.camerasecuritysystem.client

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.camerasecuritysystem.client.databinding.ConnectDialogLayoutBinding
import java.lang.Exception
import java.lang.NumberFormatException

class ConnectDialog : AppCompatDialogFragment() {

    private var listener: ConnectDialogListener? = null

    private var keyStore = KeyStoreHelper("connectToServer")

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var binding: ConnectDialogLayoutBinding

    private var cameraValid: Boolean    = false
    private var portValid:   Boolean    = false
    private var ipAddrValid: Boolean    = false
    private var passWdValid: Boolean    = false

    private var okButton: Button? = null

    private lateinit var cameraidStr:  String
    private lateinit var portStr:      String
    private lateinit var ipaddressStr: String
    private lateinit var passwordStr:  String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        binding = ConnectDialogLayoutBinding.inflate(inflater)

        this.cameraidStr    = resources.getString(R.string.camera_id_title)
        this.portStr        = resources.getString(R.string.port_title)
        this.ipaddressStr   = resources.getString(R.string.ip_address_title)
        this.passwordStr    = resources.getString(R.string.password_title)

        //Get the id of the input fields
        val editTextCameraId  = binding.editCameraId
        val editTextPort      = binding.editPort
        val editTextIPAddress = binding.editIpAddress
        val editTextPassword  = binding.editPassword

        //Get access to all shared preferences
        sharedPreferences = requireContext().getSharedPreferences(
            "com.camerasecuritysystem.client",
            Context.MODE_PRIVATE)

        //Assign the preferences to variables
        val cameraId =
            sharedPreferences.getString(resources.getString(R.string.camera_id), null)
        val port =
            sharedPreferences.getString(resources.getString(R.string.port), null)
        val ipAddress =
            sharedPreferences.getString(resources.getString(R.string.ip_address), null)
        val encPwd =
            sharedPreferences.getString(resources.getString(R.string.encPwd), null)
        val ivByte =
            sharedPreferences.getString(resources.getString(R.string.pwdIVByte), null)

        //Check if the ivByte and encrypted password exist
        if (ivByte != null && encPwd != null) {

            //Decrypt the IV byte and the password
            try {
                val pwdText = keyStore.decryptData(
                    ivByte.toByteArray(Charsets.ISO_8859_1),
                    encPwd.toByteArray(Charsets.ISO_8859_1)
                )

                //Set the password text in the input field
                binding.editPassword.setText(pwdText)
                passWdValid = true

            } catch (e: Exception) {
                Log.e("EXCEPTION", "error: ", e)
            }
        }

        //Create the dialog window
        val dialog = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Login")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Ok") { _, _ ->

                // At this point, the input values can be retrieved from the dialog
                // since the OK button is disabled until they are valid.
                val cameraIdText = editTextCameraId.text.toString()
                val portText = editTextPort.text.toString()
                val ipAddressText = editTextIPAddress.text.toString()
                val passwordText = editTextPassword.text.toString()

                //Pass the new input values to store them in the shared preferences
                listener!!.applyTexts(cameraIdText, portText, ipAddressText, passwordText)
            }
            .create()

        dialog.setOnShowListener {
            this.okButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            //Set the initial values for the input fields and update the OK button
            if (cameraId != null) {
                binding.editCameraId.setText(cameraId)
                cameraValid = true
            } else {
                binding.inputLayoutCameraID.error = String.format(resources.getString(R.string.err_not_empty), cameraidStr)
                setOkButton(okButton)
            }
            //Set the port text in the input field
            if (port != null) {
                binding.editPort.setText(port)
                portValid = true
            } else {
                binding.inputLayoutPort.error = String.format(resources.getString(R.string.err_not_empty), portStr)
                setOkButton(okButton)
            }
            //Set the IP text in the input field
            if (ipAddress != null) {
                binding.editIpAddress.setText(ipAddress)
                ipAddrValid = true
            } else {
                binding.inputLayoutIpAddress.error = String.format(resources.getString(R.string.err_not_empty), ipaddressStr)
                setOkButton(okButton)
            }
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
                    passWdValid = true

                } catch (e: Exception) {
                    Log.e("EXCEPTION", "error: ", e)
                    setOkButton(okButton)
                }
            } else {
                binding.inputLayoutPassword.error = String.format(resources.getString(R.string.err_not_empty), passwordStr)
                setOkButton(okButton)
            }

            // Add text change handlers used for input validation
            binding.editCameraId.doOnTextChanged { text, _, _, _ ->
                cameraValid = validateCameraId(text?.toString())
                setOkButton(okButton)
            }
            binding.editPort.doOnTextChanged { text, _, _, _ ->
                portValid = validatePort(text?.toString())
                setOkButton(okButton)
            }
            binding.editIpAddress.doOnTextChanged { text, _, _, _ ->
                ipAddrValid = validateIPAddress(text?.toString())
                setOkButton(okButton)
            }
            binding.editPassword.doOnTextChanged { text, _, _, _ ->
                passWdValid = validatePassword(text?.toString())
                setOkButton(okButton)
            }
        }

        // Return the dialog window
        return dialog
    }

    private fun setOkButton(okButton: Button?) {
        okButton?.isEnabled = ( cameraValid && portValid && ipAddrValid && passWdValid )
    }


    /* The following validation functions will try to sanitize the user input at least
     * somewhat, and show an appropriate error. */

    private fun validateCameraId(cameraIdText: String?): Boolean {
        val camera_til = binding.inputLayoutCameraID

        if (cameraIdText == null || cameraIdText.isEmpty()) {
            camera_til.error = String.format(resources.getString(R.string.err_not_empty), cameraidStr)
            return false
        }

        try {
            val cameraIdAsInt = cameraIdText.toInt()
            if (cameraIdAsInt < 1) {
                camera_til.error = String.format(resources.getString(R.string.err_at_least), cameraidStr, 1)
                return false
            }
        } catch (ex: NumberFormatException) {
            camera_til.error = String.format(resources.getString(R.string.err_must_be_number), cameraidStr)
            return false
        }

        camera_til.error = null
        return true
    }

    private fun validatePort(portText: String?): Boolean {
        val port_til = binding.inputLayoutPort

        if (portText == null || portText.isEmpty()) {
            port_til.error = String.format(resources.getString(R.string.err_not_empty), portStr)
            return false
        }

        try {
            val portAsInt = portText.toInt()
            if (portAsInt < 1) {
                port_til.error = String.format(resources.getString(R.string.err_at_least), portStr, 1)
                return false
            }
            if (portAsInt > 65535) {
                port_til.error = String.format(resources.getString(R.string.err_at_most), portStr, 65535)
                return false
            }
        } catch (ex: NumberFormatException) {
            port_til.error = String.format(resources.getString(R.string.err_must_be_number), portStr)
            return false
        }

        port_til.error = null
        return true
    }

    private fun validateIPAddress(ipAddressText: String?): Boolean {
        val ipAddress_til = binding.inputLayoutIpAddress

        if (ipAddressText == null || ipAddressText.isEmpty()) {
            ipAddress_til.error = String.format(resources.getString(R.string.err_not_empty), ipaddressStr)
            return false
        }

        // TODO: more thorough validation

        ipAddress_til.error = null
        return true
    }

    private fun validatePassword(passwordText: String?): Boolean {
        val password_til = binding.inputLayoutPassword

        if (passwordText == null || passwordText.isEmpty()) {
            password_til.error = String.format(resources.getString(R.string.err_not_empty), passwordStr)
            return false
        }

        // TODO: more thorough validation

        password_til.error = null
        return true
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
        fun applyTexts(cameraID: String, port: String, ipAddress: String, password: String)
    }
}
