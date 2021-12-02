package com.example.camerasecuritysystem

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.camera.camera2.Camera2Config
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.util.Log

import androidx.annotation.NonNull
import androidx.annotation.Nullable

import androidx.core.app.ActivityCompat
import java.util.concurrent.ExecutionException

import com.example.camerasecuritysystem.databinding.FragmentDashcamBinding
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import java.io.File
import java.io.IOException


class DashcamFragment(context: Context?) : Fragment(), CameraXConfig.Provider {

    val TAG: String = DashcamFragment::class.java.getSimpleName()
    private val REQUEST_PERMISSION_CAMERA = 777

//    fun newInstance(): Fragment? {
//        return DashcamFragment()
//    }

    private var binding: FragmentDashcamBinding? = null
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null

    private var fileName: String = ""

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private var videoCapture: VideoCapture? = null


    private val LOG_TAG = "AudioRecordTest"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "create view")
        binding = FragmentDashcamBinding.inflate(inflater, container, false)
        // binding.setViewModel(this.mViewModel);
        return binding!!.getRoot()
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "view created")

        //Start recording
//        startRecording()
    }

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }



    private fun startRecording() {

        Log.e(LOG_TAG, "IK KOM HIER")

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setMaxDuration(5000)

            var root = context?.filesDir


            var file = File("$root/test" + (System.currentTimeMillis() / 1000L).toString() + ".mp4")
            setOutputFile(file)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
                start()
                Log.e(LOG_TAG, "APP GESTART")
                Toast.makeText(context, "Opname gestart", Toast.LENGTH_LONG)
            } catch (e: IOException) {

                Log.e(LOG_TAG, "prepare() failed")
                Log.e(LOG_TAG, e.toString())
            }

        }
    }

    private fun stopRecording() {


        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        player?.release()
        player = null

        Toast.makeText(context, "Opname gestopt", Toast.LENGTH_SHORT)

        startRecording()
    }

    private fun startPreview() {
        Log.v(TAG, "startPreview")
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture!!.addListener({
            Log.v(TAG, "cameraProviderFuture.Listener")
            try {
                val cameraProvider = cameraProviderFuture!!.get()
                val preview = Preview.Builder()
                    .build()

                preview.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)

                videoCapture = VideoCapture.Builder().build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this.viewLifecycleOwner,
                    cameraSelector,
                    preview, videoCapture
                )
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.e(TAG, "cameraProviderFuture.Listener", e)
            } catch (e: InterruptedException) {
                Log.e(TAG, "cameraProviderFuture.Listener", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (cameraProviderFuture == null) {
                startPreview()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            for (p in permissions.indices) {
                if (Manifest.permission.CAMERA == permissions[p]) {
                    if (grantResults[p] == PackageManager.PERMISSION_GRANTED) {
                        startPreview()
                    } else {
                    }
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }


}