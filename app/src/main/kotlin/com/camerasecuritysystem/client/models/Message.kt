package com.camerasecuritysystem.client.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val type: MessageType,          // Mandatory
    val id: Int                     = 0,
    val password: String?           = null,
    val footage: ArrayList<String>? = null
)
