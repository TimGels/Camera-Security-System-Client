package com.camerasecuritysystem.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoView : VideoView
    private lateinit var videoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        videoView = findViewById(R.id.videoView)

        var mediaController = MediaController(this, false)

        videoView.setVideoPath(getVideoPath())

        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        videoView.start()

    }

    fun getVideoPath() : String? {
        return intent.getStringExtra("videoPath")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return false
    }
}