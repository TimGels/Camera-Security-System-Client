package com.camerasecuritysystem.client.models

class FootageHandler {

    companion object {
        @JvmStatic fun getAllFootage(): ArrayList<Footage> {
            //Create array that will contain all video files
            val allFootage = ArrayList<Footage>()

            //Get all footage from sandbox
            //Now test data:
            allFootage.add(Footage(filename = "video.mp4"))
            allFootage.add(Footage(filename = "video1.mp4"))

            return allFootage
        }
    }

}
