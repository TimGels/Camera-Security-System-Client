package com.camerasecuritysystem.client.models

class MessageHandler {

    companion object {
        @JvmStatic suspend fun handleMessage(message: Message, serverConnection: ServerConnection) {

            when (message.type) {
                MessageType.FOOTAGE_REQUEST_ALL -> {
                    serverConnection.sendMessage( Message(
                        type = MessageType.FOOTAGE_RESPONSE_ALL,
                        footage = FootageHandler.getAllFootage()
                    ))
                }
                MessageType.DOWNLOAD_REQUEST -> {

                }
            }
        }
    }

}
