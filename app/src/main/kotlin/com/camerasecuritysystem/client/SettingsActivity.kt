package com.camerasecuritysystem.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.camerasecuritysystem.client.databinding.ActivitySettingsBinding
import com.camerasecuritysystem.client.models.ServerConnection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(),
    ConnectDialog.ConnectDialogListener {

    private var binding: ActivitySettingsBinding? = null

    private var keyStore = KeyStoreHelper("connectToServer")

    lateinit var connectionLiveData: ConnectionLiveData

    lateinit var connectBtn : Button

    private lateinit var sharedPreferences: SharedPreferences
    var pwdIV: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        sharedPreferences =
            this.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)

        pwdIV = sharedPreferences.getString(resources.getString(R.string.pwdIVByte), "")


        val settings = findViewById<View>(R.id.textViewConnectionSettings)
        settings.setOnClickListener {
            openDialog()
        }

        connectBtn = findViewById<View>(R.id.connectBtn) as Button

        connectBtn.setOnClickListener {

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

        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, { isNetworkAvailable ->
            Log.e("NETWORK", "Connected = $isNetworkAvailable")
            updateUI()
        })
        updateUI()
    }

    private fun updateUI(){

        val isNetworkAvailable = connectionLiveData.value

        if (isNetworkAvailable == null || !isNetworkAvailable  ) {
            connectBtn.isClickable = false
            connectBtn.isEnabled = false
            connectBtn.backgroundTintList =
                getColorStateList(R.color.cardview_dark_background)

            connectBtn.text = "No internet"
        }
        else if (isNetworkAvailable == true) {
            connectBtn.isClickable = true
            connectBtn.isEnabled = true
            connectBtn.backgroundTintList =
                getColorStateList(R.color.design_default_color_primary)
            connectBtn.text = "Connect"
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
