package com.camerasecuritysystem.client.models

import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_BITRATE
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
import android.util.Log
import com.camerasecuritysystem.client.CSSApplication
import java.io.File
import java.lang.Exception
import java.lang.NumberFormatException
import kotlin.collections.ArrayList

object FootageHandler {
    private const val tag = "FootageHandler"

    fun getAllFootage(): ArrayList<Footage> {
        val context = CSSApplication.context

        // Create array that will contain all video files
        val allFootage = ArrayList<Footage>()

        // Get all footage from sandbox
        val files = File("${context.filesDir}/dashcam/").listFiles()

        files?.forEach { file ->
            allFootage.add(getFootageMetadata(file))
        }

        return allFootage
    }

    fun getFootageMetadata(file: File): Footage {
        var duration: Int? = null
        var resolution: String? = null
        var bitrate: Int? = null

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.path)

        try {
            // Retrieve video length
            val durationStr = retriever.extractMetadata(METADATA_KEY_DURATION)
            val durationInt = durationStr?.toInt()
            if (durationInt != null && durationInt >= 0 && durationInt <= Int.MAX_VALUE) {
                duration = durationInt
            }

            // Retrieve video resolution
            val width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)
            val height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)
            if (width != null && height != null) {
                resolution = "${width}x$height"
            }

            // Retrieve bitrate
            val bitrateStr = retriever.extractMetadata(METADATA_KEY_BITRATE)
            val bitrateInt = bitrateStr?.toInt()
            if (bitrateInt != null && bitrateInt >= 0 && bitrateInt <= Int.MAX_VALUE) {
                bitrate = bitrateInt
            }
        } catch (ex: NumberFormatException) {
            Log.e(tag, "NumberFormat: ${ex.message}")
        } catch (ex: Exception) {
            Log.e(tag, "Error occurred: ${ex.message}")
        }

        return Footage(
            filename = file.name,
            duration = duration,
            resolution = resolution,
            bitrate = bitrate
        )
    }
}
