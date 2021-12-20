package com.camerasecuritysystem.client

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.camerasecuritysystem.client.databinding.MailDialogLayoutBinding
import java.lang.Exception

class MailDialog : AppCompatDialogFragment() {

    private var listener: MailDialogListener? = null

    private var keyStore = KeyStoreHelper("MailAPI")

    private lateinit var binding: MailDialogLayoutBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var keyValid: Boolean = false
    private var secretValid: Boolean = false
    private var emailValid: Boolean = false

    private var okButton: Button? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        binding = MailDialogLayoutBinding.inflate(inflater)

        //Get the id of the input fields
        val editTextKey = binding.apiKeyText
        val editTextSecret = binding.apiSecretText
        val editTextEmail = binding.emailText

        //Get access to all shared preferences
        sharedPreferences = requireContext().getSharedPreferences(
            "com.camerasecuritysystem.client",
            Context.MODE_PRIVATE
        )

        //Assign the preferences to variables
        val apiKeyEnc =
            sharedPreferences.getString(resources.getString(R.string.mail_key), null)
        val apiSecretEnc =
            sharedPreferences.getString(resources.getString(R.string.mail_secret), null)
        val email =
            sharedPreferences.getString(resources.getString(R.string.email), null)

        val ivByteKey =
            sharedPreferences.getString(resources.getString(R.string.mailKeyIVByte), null)
        val ivByteSecret =
            sharedPreferences.getString(resources.getString(R.string.secretIVByte), null)

        //Decrypt key
        if (ivByteKey != null && apiKeyEnc != null) {

            //Decrypt the IV byte and the password
            try {
                val keyText = keyStore.decryptData(
                    ivByteKey.toByteArray(Charsets.ISO_8859_1),
                    apiKeyEnc.toByteArray(Charsets.ISO_8859_1)
                )

                //Set the password text in the input field
                editTextKey.setText(keyText)
                keyValid = true

            } catch (e: Exception) {
                Log.e("EXCEPTION", "error: ", e)
            }
        }

        //Decrypt secret
        if (ivByteSecret != null && apiSecretEnc != null) {

            //Decrypt the IV byte and the secret
            try {
                val secretText = keyStore.decryptData(
                    ivByteSecret.toByteArray(Charsets.ISO_8859_1),
                    apiSecretEnc.toByteArray(Charsets.ISO_8859_1)
                )

                //Set the password text in the input field
                editTextSecret.setText(secretText)
                secretValid = true

            } catch (e: Exception) {
                Log.e("EXCEPTION", "error: ", e)
            }
        }

        //Set the email text
        editTextEmail.setText(email)

        //Create the dialog window
        val dialog = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("Mailjet API")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Save") { _, _ ->

                // At this point, the input values can be retrieved from the dialog
                // since the OK button is disabled until they are valid.
                val keyText = editTextKey.text.toString()
                val secretText = editTextSecret.text.toString()
                val emailText = editTextEmail.text.toString()

                //Pass the new input values to store them in the shared preferences
                listener!!.applyTexts(keyText, secretText, emailText)
            }
            .create()

        dialog.setOnShowListener {
            this.okButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        }

        return dialog
    }

    /**
     * Check if Settingsactivity implemented ConnectDialogListener
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as MailDialog.MailDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "must implement ConnectDialogListener"
            )
        }
    }

    interface MailDialogListener {
        fun applyTexts(apiKey: String, apiSecret: String, email: String)
    }
}