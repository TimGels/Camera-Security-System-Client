package com.camerasecuritysystem.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.camerasecuritysystem.client.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity(),
    ConnectDialog.ConnectDialogListener {

    private var binding: ActivitySettingsBinding? = null

    private var keyStore = KeyStoreHelper("connectToServer")

    private lateinit var sharedPreferences: SharedPreferences
    var pwdIV: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = ActivitySettingsBinding.inflate(layoutInflater)

        sharedPreferences =
            this.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)

        pwdIV = sharedPreferences.getString("pwdIVByte", "")

        val connectionSettings = findViewById<View>(R.id.textViewConnectionSettings)
        connectionSettings.setOnClickListener {
            openDialog()
        }

        val connectBtn = findViewById<View>(R.id.connectBtn) as Button
        connectBtn.setOnClickListener {
            //TODO maak een connectie aan
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

    override fun applyTexts(cameraID: String?, port: String?, ipAddress: String?, password: String?) {
        //TODO Input validatie
        if (password != null) {
            val pair = keyStore.encryptData(password)

            sharedPreferences.edit().putString("pwdIVByte", pair.first.toString(Charsets.ISO_8859_1))
                .apply()
            sharedPreferences.edit().putString("encPwd", pair.second.toString(Charsets.ISO_8859_1))
                .apply()
        }

        //TODO Input validatie
        if (port != null) {
            sharedPreferences.edit().putString("port", port).apply()
        }

        //TODO Input validatie
        if (ipAddress != null) {
            sharedPreferences.edit().putString("ip_address", ipAddress).apply()
        }

        //TODO Input validatie
        if (cameraID != null && cameraID != "") {
            sharedPreferences.edit().putString("camera_id", cameraID).apply()
        }
    }

}