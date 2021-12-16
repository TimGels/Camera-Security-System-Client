package com.camerasecuritysystem.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.camerasecuritysystem.client.databinding.ActivityCameraBinding
import com.camerasecuritysystem.client.models.CameraMode

class CameraActivity : AppCompatActivity() {

    private var _binding: ActivityCameraBinding? = null
    private val binding get() = _binding!!

    private val TAG = "CameraActivity"

    private lateinit var mode : CameraMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        getIncomingIntent()

        Log.d(TAG, "onCreate: started")
    }

    fun getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent: checking incoming intents")

        val mode = getMode(intent)

        if (mode != null) {

            this.mode = mode

            val textView: TextView = findViewById(R.id.modeText)
            textView.text = mode.toString()
        }
    }

    fun getMode(intent: Intent): CameraMode? {
        if (intent.hasExtra("modus")) {
            var extras = intent.extras
            if ((extras != null) && (extras.containsKey("modus"))) {
                var modus: CameraMode = extras.getSerializable("modus") as CameraMode;
                return modus
            }
        }
        return null
    }

    override fun onStart() {
        super.onStart()

        //TODO Maak hier een if van
        when(mode){
            CameraMode.DASHCAM ->{
                val transaction = supportFragmentManager.beginTransaction()

                transaction.add(R.id.fragmentContainerView, DashcamFragment(), "DashcamFragment").commitAllowingStateLoss()
            }
        }
    }
}