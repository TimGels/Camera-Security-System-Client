package com.camerasecuritysystem.client

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.camerasecuritysystem.client.models.CameraMode

class CameraActivity : AppCompatActivity() {

    private val TAG = "CameraActivity"

    private lateinit var mode: CameraMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        getIncomingIntent()

        Log.d(TAG, "onCreate: started")
    }

    @SuppressLint("SetTextI18n")
    fun getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent: checking incoming intents")

        val mode = getMode(intent)

        if (mode != null) {

            this.mode = mode

            if (mode !== CameraMode.DASHCAM) {
                val textView: TextView = findViewById(R.id.modeText)
                textView.text = String.format(resources.getString(R.string.coming_soon), mode)
            }
        }
    }

    fun getMode(intent: Intent): CameraMode? {
        if (intent.hasExtra("modus")) {
            val extras = intent.extras
            if ((extras != null) && (extras.containsKey("modus"))) {
                val modus: CameraMode = extras.getSerializable("modus") as CameraMode
                return modus
            }
        }
        return null
    }

    override fun onStart() {
        super.onStart()

        when (mode) {
            CameraMode.DASHCAM -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(
                    R.id.fragmentContainerView, DashcamFragment(),
                    "DashcamFragment"
                ).commitAllowingStateLoss()
            }
            else -> Log.e(TAG, "Mode not yet implemented")
        }
    }
}
