package com.example.camerasecuritysystem

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.camerasecuritysystem.databinding.SettingsActivityBinding


class SettingsActivity : AppCompatActivity(), ConnectDialog.ConnectDialogListener {


    private var binding: SettingsActivityBinding? = null

    private var keyStore = KeyStoreHelper("connectToServer")

    private lateinit var sharedPreferences: SharedPreferences
    var pwdIV: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = SettingsActivityBinding.inflate(layoutInflater)

        sharedPreferences =
            this.getSharedPreferences("com.example.camerasecuritysystem", Context.MODE_PRIVATE)

        pwdIV = sharedPreferences.getString("pwdIVByte", "")

        val button = findViewById<View>(R.id.connectBtn) as Button
        button.setOnClickListener {
            openDialog()
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