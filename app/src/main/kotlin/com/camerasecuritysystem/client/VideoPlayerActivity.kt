package com.camerasecuritysystem.client

import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        videoView = findViewById(R.id.videoView)

        val mediaController = MediaController(this, false)

        videoView.setVideoPath(getVideoPath())

        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.start()
    }

    fun getVideoPath(): String? {
        return intent.getStringExtra("videoPath")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }
}
