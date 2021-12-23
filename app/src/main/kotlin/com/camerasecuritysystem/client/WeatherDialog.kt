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
import com.camerasecuritysystem.client.databinding.WeatherDialogLayoutBinding
import java.util.regex.Pattern

const val KEY_LENGTH_WEATHER = 34

class WeatherDialog(context: Context) : AppCompatDialogFragment() {

    private var listener: WeatherDialogListener? = null

    private var keyStore =
        KeyStoreHelper(context.resources.getString(R.string.keyStoreAliasWeather))

    private lateinit var binding: WeatherDialogLayoutBinding

    private lateinit var sharedPreferences: SharedPreferences

    private var keyValid: Boolean = false
    private var cityValid: Boolean = false

    private var okButton: Button? = null

    private lateinit var keyString: String
    private lateinit var cityString: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        binding = WeatherDialogLayoutBinding.inflate(inflater)

        // Get the id of the input fields
        val editTextKey = binding.apiKeyText
        val editTextCity = binding.cityText

        // Input field strings
        this.keyString = resources.getString(R.string.mail_key_string)
        this.cityString = resources.getString(R.string.cityString)

        // Get access to all shared preferences
        sharedPreferences = requireContext().getSharedPreferences(
            "com.camerasecuritysystem.client",
            Context.MODE_PRIVATE
        )

        val apiKeyEnc =
            sharedPreferences.getString(resources.getString(R.string.weatherApiKey), null)

        val city =
            sharedPreferences.getString(resources.getString(R.string.weatherCity), null)

        val ivByteKey =
            sharedPreferences.getString(resources.getString(R.string.weatherKeyIVByte), null)

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

        // Set the city text
        editTextCity.setText(city)

        // Create the dialog window
        val dialog = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
            .setTitle("OpenWeather API")
            .setNegativeButton("Cancel") { _, _ -> }
            .setPositiveButton("Save") { _, _ ->

                // At this point, the input values can be retrieved from the dialog
                // since the OK button is disabled until they are valid.
                val keyText = editTextKey.text.toString()
                val cityText = editTextCity.text.toString()

                // Pass the new input values to store them in the shared preferences
                listener!!.applyTexts(keyText, cityText)
            }
            .create()

        dialog.setOnShowListener {
            this.okButton = (it as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            // Set the initial values for the city field and update the OK button
            if (city != null) {
                binding.cityText.setText(city)
                cityValid = true
            } else {
                binding.cityLayout.error =
                    String.format(resources.getString(R.string.err_not_empty), keyString)
                setOkButton(okButton)
            }

            // Set the initial values for the API key field and update the OK button
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

            // Add text change handlers used for input validation
            binding.apiKeyText.doOnTextChanged { text, _, _, _ ->
                keyValid = validateKey(text.toString())
                setOkButton(okButton)
            }
            binding.cityText.doOnTextChanged { text, _, _, _ ->
                cityValid = validateCity(text.toString())
                setOkButton(okButton)
            }
        }

        return dialog
    }

    @Suppress("ReturnCount")
    private fun validateKey(key: String): Boolean {
        val layout = binding.apiKeyLayout
        if (key.length < KEY_LENGTH_WEATHER) {
            layout.error = String.format(resources.getString(R.string.err_too_short), keyString)
            return false
        }
        if (key.length > KEY_LENGTH_WEATHER) {
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
    private fun validateCity(city: String): Boolean {
        val layout = binding.cityLayout

        // Check special characters
        if (!Pattern.compile("^[a-zA-Z0-9äöüëÄÖÜËß',.\\s-]{1,50}\$", Pattern.CASE_INSENSITIVE)
            .matcher(city).find()
        ) {
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            return false
        }

        return try {
            city.toDouble()
            layout.error = String.format(resources.getString(R.string.err_invalid), keyString)
            false
        } catch (e: NumberFormatException) {
            layout.error = null
            return true
        }
    }

    private fun setOkButton(okButton: Button?) {
        okButton?.isEnabled = (keyValid && cityValid)
    }

    /**
     * Check if Settingsactivity implemented ConnectDialogListener
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as WeatherDialogListener
        } catch (e: ClassCastException) {
            Log.e("EXCEPTION", "${e.message}")
            throw ClassCastException("$context: must implement WeatherDialogListener")
        }
    }

    interface WeatherDialogListener {
        fun applyTexts(apiKey: String, city: String)
    }
}
