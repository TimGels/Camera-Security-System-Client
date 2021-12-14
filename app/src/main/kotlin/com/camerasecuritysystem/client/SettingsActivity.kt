package com.camerasecuritysystem.client

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
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


        var settings = findViewById<View>(R.id.textViewConnectionSettings)
        settings.setOnClickListener {
            openDialog()
        }

        connectBtn = findViewById<View>(R.id.connectBtn) as Button

        connectBtn.setOnClickListener {

            //Try to setup a connection
            GlobalScope.launch {
                try {
                    ServerConnection(this@SettingsActivity).connectIfPossible()
                } catch (e: Exception) {
                    Log.e("CONNECTION ERROR", "${e.message.toString()}")
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

        var isNetworkAvailable = connectionLiveData.value
        Log.e("LiveData value", "$isNetworkAvailable")

        if (isNetworkAvailable == null || !isNetworkAvailable  ) {
            connectBtn.isClickable = false
            connectBtn.isEnabled = false
            connectBtn.backgroundTintList =
                getColorStateList(R.color.cardview_dark_background)

            connectBtn.text = "NO Signal"
            Log.e("Button", "Enabled = ${connectBtn.isEnabled}")
        }

        if (isNetworkAvailable == true) {
            connectBtn.isClickable = true
            connectBtn.isEnabled = true
            connectBtn.backgroundTintList =
                getColorStateList(R.color.design_default_color_primary)
            connectBtn.text = "Connect"
            Log.e("Button", "Enabled = ${connectBtn.isEnabled}")
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

    override fun applyTexts(
        cameraID: String?,
        port: String?,
        ipAddress: String?,
        password: String?
    ) {
        //TODO Input validatie
        if (password != null) {
            val pair = keyStore.encryptData(password)

            sharedPreferences.edit().putString(
                resources.getString(R.string.pwdIVByte),
                pair.first.toString(Charsets.ISO_8859_1)
            )
                .apply()
            sharedPreferences.edit().putString(
                resources.getString(R.string.encPwd),
                pair.second.toString(Charsets.ISO_8859_1)
            )
                .apply()
        }

        //TODO Input validatie
        if (port != null) {
            sharedPreferences.edit().putString(resources.getString(R.string.port), port).apply()
        }

        //TODO Input validatie
        if (ipAddress != null) {
            sharedPreferences.edit().putString(resources.getString(R.string.ip_address), ipAddress)
                .apply()
        }

        //TODO Input validatie
        if (cameraID != null && cameraID != "") {
            sharedPreferences.edit().putString(resources.getString(R.string.camera_id), cameraID)
                .apply()
        }
    }

}