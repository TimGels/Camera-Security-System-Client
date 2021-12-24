package com.camerasecuritysystem.client.models

import kotlinx.serialization.Serializable

/**
 * Holds information about a video file.
 * @property filename The name of the video file
 * @property duration Length of the video in ms
 * @property resolution Formatted as WIDTHxHEIGHT, both in pixels
 * @property bitrate Bitrate of the video in bits / second
 */
@Serializable
data class Footage(
    val filename: String,
    val duration: Int? = null,
    val resolution: String? = null,
    val bitrate: Int? = null
)
