package com.camerasecuritysystem.client

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
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
import androidx.camera.video.ActiveRecording
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.camerasecuritysystem.client.databinding.FragmentDashcamBinding
import com.camerasecuritysystem.client.models.IWeatherAPIService
import com.camerasecuritysystem.client.models.Weather
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.properties.Delegates

const val OPEN_WEATHER_BASE_URL: String = "https://api.openweathermap.org/"

const val TAG: String = "DEBUG TEXT"

const val DIFF_KELVIN_CELSIUS: Float = 273.15F

class DashcamFragment : Fragment() {

    val networkResponse: MutableLiveData<Float> = MutableLiveData()

    private var binding: FragmentDashcamBinding? = null

    private var activeRecording: ActiveRecording? = null
    private lateinit var recordingState: VideoRecordEvent
    private lateinit var videoCapture: VideoCapture<Recorder>
    private val mainThreadExecutor by lazy { ContextCompat.getMainExecutor(requireContext()) }

    private var timeLimitFragment by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences =
            requireContext().getSharedPreferences(
                "com.camerasecuritysystem.client",
                Context.MODE_PRIVATE
            )

        networkResponse.observe(viewLifecycleOwner, {
            binding?.temperatureField?.text = it.toString()
        })

        timeLimitFragment = sharedPreferences.getInt(
            resources.getString(R.string.fragment_recording_seconds),
            resources.getInteger(R.integer.default_fragment_recording_seconds)
        )

        binding = FragmentDashcamBinding.inflate(inflater, container, false)

        requestTemperature()

        return binding!!.root
    }

    private fun requestTemperature() = CoroutineScope(Dispatchers.IO).launch {
        val retrofit = Retrofit.Builder()
            .baseUrl(OPEN_WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: IWeatherAPIService = retrofit.create(IWeatherAPIService::class.java)

        val main: Call<Weather> =
            service.listWeather("", "")

        main.enqueue(object : Callback<Weather> {
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful) {
                    val tempKelvin = response.body()?.main?.temp

                    if (tempKelvin == null || tempKelvin < 0) {
                        Log.e(
                            TAG,
                            "Error detected while retrieving results from API: " +
                                    OPEN_WEATHER_BASE_URL
                        )
                        Toast.makeText(
                            requireContext(),
                            "Error detected while retrieving results from API",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val temp =
                        BigDecimal(tempKelvin.minus(DIFF_KELVIN_CELSIUS).toString()).setScale(
                            2, RoundingMode.HALF_UP
                        ).toFloat()
                    networkResponse.postValue(temp)
                } else {
                    Toast.makeText(requireContext(), "City not found", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Weather>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
        enableUI(false) // Our eventListener will turn on the Recording UI.
        // React to user touching the capture button
        binding?.captureButton?.apply {
            setOnClickListener {
                if (!this@DashcamFragment::recordingState.isInitialized ||
                    recordingState is VideoRecordEvent.Finalize
                ) {
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
    @SuppressLint("SwitchIntDef")
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

        if (time > timeLimitFragment) {
            val recording = activeRecording
            if (recording != null) {
                Log.e(TAG, stats.recordedDurationNanos.toString())
                activeRecording?.stop()
                startRecording()
            } else {
                throw Exception("Unexpected state of recording")
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
