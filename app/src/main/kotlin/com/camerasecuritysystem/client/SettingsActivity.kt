package com.camerasecuritysystem.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.camerasecuritysystem.client.databinding.ActivitySettingsBinding
import com.camerasecuritysystem.client.models.ServerConnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(),
    ConnectDialog.ConnectDialogListener {

    private lateinit var binding: ActivitySettingsBinding

    private var keyStore = KeyStoreHelper("connectToServer")

    lateinit var connectionLiveData: ConnectionLiveData

    private var isFragmentDurationValid: Boolean = false

    private lateinit var sharedPreferences: SharedPreferences
    var pwdIV: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sharedPreferences =
            this.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)

        pwdIV = sharedPreferences.getString(resources.getString(R.string.pwdIVByte), "")

        binding.textViewConnectionSettings.setOnClickListener {
            openDialog()
        }

        binding.saveButton.setOnClickListener {
            saveSettings()
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.connectBtn.setOnClickListener {
            val context = this.applicationContext

            //Try to setup a connection
            GlobalScope.launch {
                try {
                    val connection = ServerConnection.getInstance()
                    if (!connection.isConnected()){
                        connection.connectIfPossible(context)

                        Log.e("Connection", "Connecting is possible: ${connection.connectIfPossible(context)}")
                    }else{
                        Log.e("Connection", "Already connected")
                    }

                } catch (e: Exception) {
                    Log.e("CONNECTION ERROR", e.message.toString())
                }
            }
        }
        initializeUISettings()
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, { isNetworkAvailable ->
            Log.e("NETWORK", "Connected = $isNetworkAvailable")
            updateUI()
        })
        updateUI()
    }

    private fun updateUI() {

        val isNetworkAvailable = connectionLiveData.value

        if (isNetworkAvailable == null || !isNetworkAvailable) {
            binding.connectBtn.isClickable = false
            binding.connectBtn.isEnabled = false
            binding.connectBtn.backgroundTintList =
                getColorStateList(R.color.cardview_dark_background)

            binding.connectBtn.text = "No internet"
        } else if (isNetworkAvailable == true) {
            binding.connectBtn.isClickable = true
            binding.connectBtn.isEnabled = true
            binding.connectBtn.backgroundTintList =
                getColorStateList(R.color.design_default_color_primary)
            binding.connectBtn.text = "Connect"
        }

    }

    private fun openDialog() {
        val connectDialog = ConnectDialog()
        connectDialog.show(supportFragmentManager, "connect dialog")
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

        // Encrypt the password
        val pair = keyStore.encryptData(password)

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

        sharedPreferences.edit().putString(resources.getString(R.string.ip_address), ipAddress).apply()

        sharedPreferences.edit().putString(resources.getString(R.string.camera_id), cameraID).apply()
    }
}
