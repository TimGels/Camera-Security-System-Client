package com.camerasecuritysystem.client

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.widget.doOnTextChanged
import com.camerasecuritysystem.client.databinding.MailDialogLayoutBinding
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.regex.Pattern

const val KEY_LENGTH_MAIL = 32
const val SECRET_LENGTH = 32

class MailDialog(context: Context) : AppCompatDialogFragment() {

    private var listener: MailDialogListener? = null

    private var keyStore =
        KeyStoreHelper(context.resources.getString(R.string.keyStoreAliasMail))

    private lateinit var binding: MailDialogLayoutBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var keyValid: Boolean = false
    private var secretValid: Boolean = false
    private var emailValid: Boolean = false

    private var okButton: Button? = null

    // Descriptor of input field
    private lateinit var keyString: String
    private lateinit var secretString: String
    private lateinit var emailString: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        binding = MailDialogLayoutBinding.inflate(inflater)

        // Get the id of the input fields
        val editTextKey = binding.apiKeyText
        val editTextSecret = binding.apiSecretText
        val editTextEmail = binding.emailText

        // Input field strings
        this.keyString = resources.getString(R.string.mail_key_string)
        this.secretString = resources.getString(R.string.mail_secret_string)
        this.emailString = resources.getString(R.string.mail_email_string)

        // Get access to all shared preferences
        sharedPreferences = requireContext().getSharedPreferences(
            "com.camerasecuritysystem.client",
            Context.MODE_PRIVATE
        )

        // Assign the preferences to variables
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

        // Decrypt key
        if (ivByteKey != null && apiKeyEnc != null) {

            // Decrypt the IV byte and the password
            try {
                val keyText = keyStore.decryptData(
                    ivByteKey.toByteArray(Charsets.ISO_8859_1),
                    apiKeyEnc.toByteArray(Charsets.ISO_8859_1)
                )
                // Set the password text in the input field
                editTextKey.setText(keyText)
                keyValid = true
            } catch (e: Exception) {
                Log.e("EXCEPTION", "error: ", e)
            }
        }

        // Decrypt secret
        if (ivByteSecret != null && apiSecretEnc != null) {

            // Decrypt the IV byte and the secret
            try {
                val secretText = keyStore.decryptData(
                    ivByteSecret.toByteArray(Charsets.ISO_8859_1),
                    apiSecretEnc.toByteArray(Charsets.ISO_8859_1)
                )

                // Set the password text in the input field
                editTextSecret.setText(secretText)
                secretValid = true
            } catch (e: Exception) {
                Log.e("EXCEPTION", "error: ", e)
            }
        }

        // Set the email text
        editTextEmail.setText(email)

        // Create the dialog window
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

                // Pass the new input values to store them in the shared preferences
                listener!!.applyTexts(keyText, secretText, emailText)
            }
            .create()

        dialog.setOnShowListener {
            this.okButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            // Set the initial values for the input fields and update the OK button
            if (email != null) {
                binding.emailText.setText(email)
                emailValid = true
            } else {
                binding.emailLayout.error =
                    String.format(resources.getString(R.string.err_not_empty), emailString)
                setOkButton(okButton)
            }

            // Set the initial values for the input fields and update the OK button
            if (ivByteKey != null && apiKeyEnc != null) {

                // Decrypt the IV byte and the key
                try {
                    val keyText = keyStore.decryptData(
                        ivByteKey.toByteArray(Charsets.ISO_8859_1),
                        apiKeyEnc.toByteArray(Charsets.ISO_8859_1)
                    )

                    // Set the password text in the input field
                    editTextKey.setText(keyText)
                    keyValid = true
                } catch (e: Exception) {
                    Log.e("EXCEPTION", "error: ", e)
                    setOkButton(okButton)
                }
            } else {
                binding.apiKeyLayout.error =
                    String.format(resources.getString(R.string.err_not_empty), keyString)
                setOkButton(okButton)
            }

            // Set the initial values for the input fields and update the OK button
            if (ivByteSecret != null && apiSecretEnc != null) {

                // Decrypt the IV byte and the secret
                try {
                    val secretText = keyStore.decryptData(
                        ivByteSecret.toByteArray(Charsets.ISO_8859_1),
                        apiSecretEnc.toByteArray(Charsets.ISO_8859_1)
                    )

                    // Set the password text in the input field
                    editTextKey.setText(secretText)
                    keyValid = true
                } catch (e: Exception) {
                    Log.e("EXCEPTION", "error: ", e)
                    setOkButton(okButton)
                }
            } else {
                binding.apiSecretLayout.error =
                    String.format(resources.getString(R.string.err_not_empty), secretString)
                setOkButton(okButton)
            }

            // Add text change handlers used for input validation
            binding.apiKeyText.doOnTextChanged { text, _, _, _ ->
                keyValid = validateKey(text.toString())
                setOkButton(okButton)
            }
            binding.apiSecretText.doOnTextChanged { text, _, _, _ ->
                secretValid = validateSecret(text.toString())
                setOkButton(okButton)
            }
            binding.emailText.doOnTextChanged { text, _, _, _ ->
                emailValid = validateEmail(text.toString())
                setOkButton(okButton)
            }
        }
        return dialog
    }

    @Suppress("ReturnCount")
    private fun validateKey(key: String): Boolean {
        val layout = binding.apiKeyLayout
        if (key.length < KEY_LENGTH_MAIL) {
            layout.error = String.format(resources.getString(R.string.err_too_short), keyString)
            return false
        }
        if (key.length > KEY_LENGTH_MAIL) {
            layout.error = String.format(resources.getString(R.string.err_too_long), keyString)
            return false
        }
        if (key.isEmpty()) {
            layout.error = String.format(resources.getString(R.string.err_not_empty), keyString)
            return false
        }

        // Check special characters
        if (Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE).matcher(key).find()) {
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            return false
        }

        if (key.all { it.isLetter() }) {
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            return false
        }

        return try {
            key.toDouble()
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            false
        } catch (e: NumberFormatException) {
            layout.error = null
            return true
        }
    }

    @Suppress("ReturnCount")
    private fun validateSecret(secret: String): Boolean {
        val layout = binding.apiSecretLayout
        if (secret.length < SECRET_LENGTH) {
            layout.error = String.format(resources.getString(R.string.err_too_short), secretString)
            return false
        }

        if (secret.length > SECRET_LENGTH) {
            layout.error = String.format(resources.getString(R.string.err_too_long, secretString))
            return false
        }

        if (secret.isEmpty()) {
            layout.error = String.format(resources.getString(R.string.err_not_empty), secretString)
            return false
        }

        if (secret.all { it.isLetter() }) {
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            return false
        }

        // Check special characters
        if (Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE).matcher(secret).find()) {
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            return false
        }

        return try {
            secret.toDouble()
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            false
        } catch (e: NumberFormatException) {
            layout.error = null
            return true
        }
    }

    @Suppress("ReturnCount")
    private fun validateEmail(email: String): Boolean {
        val layout = binding.emailLayout

        if (email.isEmpty()) {
            layout.error = String.format(resources.getString(R.string.err_not_empty), emailString)
            return false
        }

        if (!email.contains("@")) {
            layout.error = String.format(resources.getString(R.string.err_invalid), emailString)
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layout.error = String.format(resources.getString(R.string.err_invalid), emailString)
            return false
        }

        layout.error = null
        return true
    }

    private fun setOkButton(okButton: Button?) {
        okButton?.isEnabled = (keyValid && secretValid && emailValid)
    }

    /**
     * Check if Settingsactivity implemented ConnectDialogListener
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as MailDialogListener
        } catch (e: ClassCastException) {
            Log.e("EXCEPTION", "${e.message}")
            throw ClassCastException("$context: must implement ConnectDialogListener")
        }
    }

    interface MailDialogListener {
        fun applyTexts(apiKey: String, apiSecret: String, email: String)
    }
}
