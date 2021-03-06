package com.camerasecuritysystem.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.camerasecuritysystem.client.databinding.ActivitySettingsBinding
import com.camerasecuritysystem.client.models.ConnectionState
import com.camerasecuritysystem.client.models.ServerConnection

class SettingsActivity : AppCompatActivity(),
    ConnectDialog.ConnectDialogListener,
    MailDialog.MailDialogListener,
    WeatherDialog.WeatherDialogListener {

    private lateinit var binding: ActivitySettingsBinding

    // TODO koppelen aan strings.xml
    private var keyStoreServer = KeyStoreHelper("connectToServer")
    private var keyStoreMail = KeyStoreHelper("MailAPI")
    private var keyStoreWeather =
        KeyStoreHelper("weatherApiKey")

    lateinit var connectionLiveData: ConnectionLiveData
    lateinit var serverLiveData: ServerLiveData

    private var isFragmentDurationValid: Boolean = false

    private lateinit var sharedPreferences: SharedPreferences
    var pwdIV: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences =
            this.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)

        pwdIV = sharedPreferences.getString(resources.getString(R.string.pwdIVByte), "")

        binding.textViewConnectionSettings.setOnClickListener {
            openConnectionDialog()
        }

        binding.saveButton.setOnClickListener {
            saveSettings()
        }

        binding.mailSettingsButton.setOnClickListener {
            openMailDialog()
        }

        binding.weatherSettingsButton.setOnClickListener {
            openWeatherDialog()
        }

        binding.connectBtn.setOnClickListener {
            if (serverLiveData.value == null) {
                Log.e("connectOnclick", "ConnectBtn clicked with no state!")
            } else if (serverLiveData.value == ConnectionState.CLOSED ||
                serverLiveData.value == ConnectionState.NO_CREDENTIALS
            ) {
                // Setup a connection.
                ServerConnection.getInstance().connectIfPossible()
            } else if (serverLiveData.value == ConnectionState.CONNECTED) {
                // Close the connection.
                ServerConnection.getInstance().close()
            }
        }

        serverLiveData = ServerLiveData()
        serverLiveData.observe(this) { state ->
            Log.e("SERVERCONN", "state: $state")
            updateUI()
        }

        initializeUISettings()

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, { isNetworkAvailable ->
            Log.e("NETWORK", "hasInternet = $isNetworkAvailable")

            // TODO: Check for autostart==true
            if (serverLiveData.value == ConnectionState.CLOSED) {
                ServerConnection.getInstance().connectIfPossible()
            }

            updateUI()
        })
        updateUI()
    }

    private fun updateUI() {
        val hasInternet = connectionLiveData.value
        val connectionState = serverLiveData.value

        if (hasInternet == null) {
            return
        }

        if (hasInternet) {
            if (connectionState != null) {
                when (connectionState) {
                    ConnectionState.NO_CREDENTIALS -> {
                        // Credentials cannot be cleared once entered, only updated.
                        binding.connectBtn.isVisible = false
                    }
                    ConnectionState.CLOSED -> {
                        setConnect()
                    }
                    ConnectionState.CONNECTING -> {
                        setConnecting()
                    }
                    ConnectionState.CONNECTED -> {
                        setConnected()
                    }
                }
            } else {
                Log.e("state", "NULL")
                setConnect()
            }
        } else {
            setNoInternet()
        }
    }

    private fun setNoInternet() {
        binding.connectBtn.isClickable = false
        binding.connectBtn.isEnabled = false
        binding.connectBtn.backgroundTintList =
            getColorStateList(R.color.design_default_color_error)
        binding.connectBtn.text = resources.getString(R.string.no_internet)
    }

    private fun setConnect() {
        binding.connectBtn.isClickable = true
        binding.connectBtn.isEnabled = true
        binding.connectBtn.backgroundTintList =
            getColorStateList(R.color.design_default_color_primary)
        binding.connectBtn.text = resources.getString(R.string.connect)
    }

    private fun setConnecting() {
        binding.connectBtn.isClickable = false
        binding.connectBtn.isEnabled = false
        binding.connectBtn.backgroundTintList =
            getColorStateList(R.color.cardview_dark_background)
        binding.connectBtn.text = resources.getString(R.string.connecting)
    }

    private fun setConnected() {
        binding.connectBtn.isClickable = true
        binding.connectBtn.isEnabled = true
        binding.connectBtn.backgroundTintList =
            getColorStateList(R.color.design_default_color_primary_dark)
        binding.connectBtn.text = resources.getString(R.string.disconnect)
    }

    private fun openConnectionDialog() {
        val connectDialog = ConnectDialog()
        connectDialog.show(supportFragmentManager, "connect dialog")
    }

    private fun openMailDialog() {
        val mailDialog = MailDialog(applicationContext)
        mailDialog.show(supportFragmentManager, "mail dialog")
    }

    private fun openWeatherDialog() {
        val weatherDialog = WeatherDialog(applicationContext)
        weatherDialog.show(supportFragmentManager, "weather dialog")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    private fun initializeUISettings() {
        initializeFragmentRecordingLengthElement()
    }

    private fun initializeFragmentRecordingLengthElement() {
        val fragmentRecordingSeconds: String = sharedPreferences.getInt(
            resources.getString(R.string.fragment_recording_seconds),
            resources.getInteger(R.integer.default_fragment_recording_seconds)
        ).toString()

        binding.recordingSecondsInput.setText(fragmentRecordingSeconds)

        binding.recordingSecondsInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isFragmentDurationValid = validateFragmentRecordingLengthElement(s.toString())
            }
        })
        isFragmentDurationValid = validateFragmentRecordingLengthElement(fragmentRecordingSeconds)
    }

    private fun validateFragmentRecordingLengthElement(value: String?): Boolean {
        if (value == null) {
            Log.w("Empty", "Encountered unexpected NULL value.")
            return false
        }

        if (value.isBlank()) {
            binding.fragmentLengthLayout.error =
                String.format(resources.getString(R.string.err_not_empty), "Input")
            return false
        }

        if (value.toIntOrNull() == null) {
            binding.fragmentLengthLayout.error =
                String.format(
                    resources.getString(R.string.err_must_be_number),
                    "Input"
                )
            return false
        }

        if (value.toIntOrNull()!! < 1) {
            binding.fragmentLengthLayout.error =
                String.format(
                    resources.getString(R.string.err_at_least),
                    "Input", 1
                )
            return false
        }

        binding.fragmentLengthLayout.error = null
        return true
    }

    private fun saveSettings() {
        if (!isFragmentDurationValid) {
            Toast.makeText(
                this,
                "Cannot save invalid settings.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        saveFragmentRecordingLength()
        finish()
    }

    private fun saveFragmentRecordingLength() {
        try {
            sharedPreferences.edit().putInt(
                resources.getString(R.string.fragment_recording_seconds),
                binding.recordingSecondsInput.text.toString().toInt()
            ).apply()
        } catch (ex: NumberFormatException) {
            Toast.makeText(
                this,
                "Failed to save fragment recording length.", Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun applyTexts(cameraID: String, port: String, ipAddress: String, password: String) {
        // NO_CREDENTIAL state can only happen first time around.
        if (serverLiveData.value == ConnectionState.NO_CREDENTIALS) {
            binding.connectBtn.isVisible = true
            setConnect()
        }

        // Encrypt the password
        val pair = keyStoreServer.encryptData(password)

        // Store the IV bytes encrypted and the password
        sharedPreferences.edit().putString(
            resources.getString(R.string.pwdIVByte),
            pair.first.toString(Charsets.ISO_8859_1)
        ).apply()
        sharedPreferences.edit().putString(
            resources.getString(R.string.encPwd),
            pair.second.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit().putString(resources.getString(R.string.port), port).apply()
        sharedPreferences.edit().putString(resources.getString(R.string.ip_address), ipAddress)
            .apply()
        sharedPreferences.edit().putString(resources.getString(R.string.camera_id), cameraID)
            .apply()
    }

    override fun applyEmailTexts(
        apiKey: String,
        apiSecret: String,
        fromEmail: String,
        toEmail: String
    ) {
        // Encrypt the key
        val pairKey = keyStoreMail.encryptData(apiKey)

        // Encrypt the secret
        val pairSecret = keyStoreMail.encryptData(apiSecret)

        // Store the IV bytes and the key and secret
        sharedPreferences.edit().putString(
            resources.getString(R.string.mailKeyIVByte),
            pairKey.first.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit().putString(
            resources.getString(R.string.mail_key),
            pairKey.second.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit().putString(
            resources.getString(R.string.secretIVByte),
            pairSecret.first.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit().putString(
            resources.getString(R.string.mail_secret),
            pairSecret.second.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit()
            .putString(resources.getString(R.string.fromEmail), fromEmail)
            .apply()
        sharedPreferences.edit()
            .putString(resources.getString(R.string.toEmail), toEmail)
            .apply()
    }

    override fun applyTexts(apiKey: String, city: String) {
        // Encrypt the key
        val pairKey = keyStoreWeather.encryptData(apiKey)

        // Store the IV bytes and the key and secret
        sharedPreferences.edit().putString(
            resources.getString(R.string.weatherKeyIVByte),
            pairKey.first.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit().putString(
            resources.getString(R.string.weatherApiKey),
            pairKey.second.toString(Charsets.ISO_8859_1)
        ).apply()

        sharedPreferences.edit().putString(resources.getString(R.string.weatherCity), city).apply()
    }
}
