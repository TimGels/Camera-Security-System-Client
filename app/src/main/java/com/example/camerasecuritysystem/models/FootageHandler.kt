package com.example.camerasecuritysystem.models

class FootageHandler {

    companion object {
        @JvmStatic fun getAllFootage(): ArrayList<String> {
            //Create array that will contain all video files
            var allFootage = ArrayList<String>()

            //Get all footage from sandbox
            //Now test data:
            allFootage.add("video.mp4")
            allFootage.add("video1.mp4")

            return allFootage
        }
    }

}
