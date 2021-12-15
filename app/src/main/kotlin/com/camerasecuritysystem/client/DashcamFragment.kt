package com.camerasecuritysystem.client

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Nullable
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.camerasecuritysystem.client.databinding.FragmentDashcamBinding
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class DashcamFragment : Fragment() {

    private var binding: FragmentDashcamBinding? = null
    private var TAG = "DEBUG TEXT"

    private var activeRecording: ActiveRecording? = null
    private lateinit var recordingState: VideoRecordEvent
    private lateinit var videoCapture: VideoCapture<Recorder>
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashcamBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCameraFragment()
    }

    /**
     * One time initialize for CameraFragment, starts when view is created.
     */
    private fun initCameraFragment() {
        initializeUI()
        viewLifecycleOwner.lifecycleScope.launch {
            bindCaptureUsecase()
        }
    }

    /**
     * Initialize the UI components
     */
    private fun initializeUI() {
        enableUI(false)  // Our eventListener will turn on the Recording UI.
        // React to user touching the capture button
        binding?.captureButton?.apply {
            setOnClickListener {
                if (!this@DashcamFragment::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize) {
                    binding!!.captureButton.setImageResource(R.drawable.ic_stop)
                    startRecording()
                } else {
                    when (recordingState) {
                        is VideoRecordEvent.Start -> {
                            activeRecording?.stop()
                            binding!!.captureButton.setImageResource(R.drawable.ic_start)
                            stopRecording()
                        }
                        else -> {
                            Log.e(
                                TAG,
                                "Unknown State ($recordingState) when Capture Button is pressed "
                            )
                        }
                    }
                }
            }
            isEnabled = false
        }
    }

    /**
     * Stop the recording of the video
     */
    private fun stopRecording() {

        val recording = activeRecording
        if (recording != null) {
            Log.i(TAG, "Stopped recording")
            recording.stop()
            activeRecording = null
        }
        binding!!.captureButton.setImageResource(R.drawable.ic_start)
    }

    /**
     *   Always bind preview + video capture use case combinations in this sample
     *   (VideoCapture can work on its own). The function should always execute on
     *   the main thread.
     */
    private fun bindCaptureUsecase() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
            )
        )

        val qualitySelector = QualitySelector
            .firstTry(QualitySelector.QUALITY_FHD)
            .finallyTry(
                QualitySelector.QUALITY_LOWEST,
                QualitySelector.FALLBACK_STRATEGY_LOWER
            )

        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding!!.previewView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, videoCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "startCamera: ", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Kick start the video recording.
     * After this function, user could start/stop recording and application listens
     * to VideoRecordEvent for the current recording status.
     */
    private fun startRecording() {
        val videoFile = File(
            requireContext().filesDir, "/dashcam/Recording-" +
                    SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ss-SSS", Locale.US
                    ).format(System.currentTimeMillis()) + ".mp4"
        )

        val videoOutput = FileOutputOptions.Builder(videoFile)
            .build()

        // configure Recorder and Start recording to the application data store.
        activeRecording =
            videoCapture.output.prepareRecording(requireActivity(), videoOutput)
                .withEventListener(
                    mainThreadExecutor,
                    captureListener,
                )
                .apply { }
                .start()
        Log.i(TAG, "Recording started")
    }

    /**
     * CaptureEvent listener.
     */
    private val captureListener = Consumer<VideoRecordEvent> { event ->
        // cache the recording state
        if (event !is VideoRecordEvent.Status)
            recordingState = event

        if (event is VideoRecordEvent.Finalize) {
            val options = event.outputOptions
            when (event.error) {
                VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE -> if (options is FileOutputOptions) {
                    Log.e(TAG, "Insufficient storage space available")
                    Toast.makeText(
                        requireContext(),
                        "Insufficient storage space available",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        splitRecording(event)
    }

    /**
     * Splits the recording when the user defined record time is reached.
     */
    private fun splitRecording(event: VideoRecordEvent) {
        val stats = event.recordingStats
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)

        // TODO: Use user defined seconds
        if (time > 10) {
            val recording = activeRecording
            if (recording != null) {
                Log.e(TAG, stats.recordedDurationNanos.toString())
                activeRecording?.stop()
                startRecording()
            } else {
                throw RuntimeException("Unexpected state of recording")
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) {
                // Enable UI elements
                enableUI(true)
            } else {
                Log.i(TAG, "Request for permissions denied")
                Toast.makeText(
                    requireContext(),
                    "You have not accepted all the permissions",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    /**
     * Enable/disable UI elements.
     */
    private fun enableUI(enable: Boolean) {
        arrayOf(
            binding!!.captureButton,
        ).forEach {
            it.isEnabled = enable
        }
    }
}