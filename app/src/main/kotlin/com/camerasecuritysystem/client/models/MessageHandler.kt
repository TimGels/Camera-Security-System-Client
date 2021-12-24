package com.camerasecuritysystem.client.models

import android.util.Log

object MessageHandler {
    private const val tag = "MessageHandler"

    suspend fun handleMessage(message: Message, serverConnection: ServerConnection) {
        when (message.type) {
            MessageType.FOOTAGE_REQUEST_ALL -> {
                // Get all footage and send the response
                serverConnection.sendMessage(
                    Message(
                        type = MessageType.FOOTAGE_RESPONSE_ALL,
                        footage = FootageHandler.getAllFootage()
                    )
                )
            }
            else -> Log.d(tag, "Not handling incoming message of type ${message.type}!")
        }
    }
}
