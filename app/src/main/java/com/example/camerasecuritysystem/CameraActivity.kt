package com.example.camerasecuritysystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.camerasecuritysystem.databinding.ActivityCameraBinding


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

//      Verander tekst
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

        when(mode){
            CameraMode.DASHCAM ->{
                val transaction = supportFragmentManager.beginTransaction()

                transaction.add(R.id.fragmentContainerView, DashcamFragment(), "DashcamFragment").commitAllowingStateLoss()
            }
        }
    }
}