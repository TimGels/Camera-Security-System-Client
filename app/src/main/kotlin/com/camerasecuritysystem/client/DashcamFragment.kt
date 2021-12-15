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

class DashcamFragment() : Fragment() {

    private var binding: FragmentDashcamBinding? = null
    private var TAG = "DEBUG TEXT"

    private var activeRecording: ActiveRecording? = null
    private lateinit var recordingState: VideoRecordEvent
    private lateinit var videoCapture: VideoCapture<Recorder>
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashcamBinding.inflate(inflater, container, false)

        return binding!!.getRoot()
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

    private fun initializeUI() {
        // React to user touching the capture button
        binding?.captureButton?.apply {
            setOnClickListener {
                if (!this@DashcamFragment::recordingState.isInitialized || recordingState is VideoRecordEvent.Finalize) {
                    enableUI(false)  // Our eventListener will turn on the Recording UI.
                    startRecording()
                } else {
                    when (recordingState) {
                        is VideoRecordEvent.Start -> {
                            activeRecording?.stop()
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
        enableUI(true)
    }

    /**
     * Kick start the video recording.
     * After this function, user could start/stop recording and application listens
     * to VideoRecordEvent for the current recording status.
     */
    private fun startRecording() {
        val videoFile = File(
            requireContext().filesDir, "Recording-" +
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
        updateUI(event);
    }

    /**
     * Update the UI according to CameraX VideoRecordEvent type:
     */
    private fun updateUI(event: VideoRecordEvent) {
        val state = if (event is VideoRecordEvent.Status) recordingState.getName()
        else event.getName()
        when (event) {
            is VideoRecordEvent.Start -> {
                showUI(UiState.RECORDING)
            }
            is VideoRecordEvent.Finalize -> {
                showUI(UiState.FINALIZED)
            }
        }

        val stats = event.recordingStats
        val size = stats.numBytesRecorded / 1000
        val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
        // TODO: Important part, check how they keep track of time as we may need this
        var text = "${state}: recorded ${size}KB, in ${time}second"
        if (event is VideoRecordEvent.Finalize)
            text = "${text}\nFile saved to: ${event.outputResults.outputUri}"

        Log.i(TAG, "recording event: $text")
    }

    /**
     * initialize UI for recording:
     */
    private fun showUI(state: UiState) {
        binding!!.let {
            when (state) {
                UiState.IDLE -> {
                    it.captureButton.setImageResource(R.drawable.ic_start)
                }
                UiState.RECORDING -> {
                    it.captureButton.setImageResource(R.drawable.ic_stop)
                    it.captureButton.isEnabled = true
                }
                UiState.FINALIZED -> {
                    it.captureButton.setImageResource(R.drawable.ic_start)
                }
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (!permissions.all { it.value }) {
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

// Camera UI  states and inputs
enum class UiState {
    IDLE,
    RECORDING,
    FINALIZED,
}

/**
 * A helper extended function to get the name(string) for the VideoRecordEvent.
 */
fun VideoRecordEvent.getName(): String {
    return when (this) {
        is VideoRecordEvent.Status -> "Status"
        is VideoRecordEvent.Start -> "Started"
        is VideoRecordEvent.Finalize -> "Finalized"
        else -> throw IllegalArgumentException()
    }
}