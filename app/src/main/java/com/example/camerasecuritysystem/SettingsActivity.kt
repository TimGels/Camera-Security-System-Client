package com.example.camerasecuritysystem

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.camerasecuritysystem.databinding.SettingsActivityBinding


class SettingsActivity : AppCompatActivity(), ConnectDialog.ConnectDialogListener {


    private var binding : SettingsActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding = SettingsActivityBinding.inflate(layoutInflater)

        val button = findViewById<View>(R.id.connectBtn) as Button
        button.setOnClickListener {
            openDialog()
        }
    }

    fun openDialog() {
        val connectDialog = ConnectDialog()
        connectDialog.show(supportFragmentManager, "connect dialog")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }

    override fun applyTexts(username: String?, password: String?) {
        //TODO hier kan er iets met de ingevoerde data gedaan worden
        Log.e("Texts", "name: $username and pwd: $password")
    }

//    class SettingsFragment : PreferenceFragmentCompat() {
//        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//            setPreferencesFromResource(R.xml.root_preferences, rootKey)
//        }
//    }
}