package com.camerasecuritysystem.client.models

import com.camerasecuritysystem.client.CSSApplication
import java.io.File

object FootageHandler {
    fun getAllFootage(): ArrayList<Footage> {
        val context = CSSApplication.context

        // Create array that will contain all video files
        val allFootage = ArrayList<Footage>()

        // Get all footage from sandbox
        val files = File("${context.filesDir}/dashcam/").listFiles()

        files?.forEach { file ->
            allFootage.add(Footage(file.name))
        }

        return allFootage
    }
}
