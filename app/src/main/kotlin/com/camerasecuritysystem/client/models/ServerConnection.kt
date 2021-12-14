package com.camerasecuritysystem.client.models

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.FrameType
import io.ktor.http.cio.websocket.readBytes
import io.ktor.network.sockets.ConnectTimeoutException
import java.nio.channels.ClosedChannelException
import java.nio.channels.UnresolvedAddressException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ServerConnection {

    private val tag: String = "initializeConnection"

    private var port: Int = 5042
    private var hostname: String = "192.168.1.147"

    private var client: HttpClient = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 100 * 1000
        }
    }

    private var serverConnection: ServerConnection? = null
    private var webSocketSession: DefaultClientWebSocketSession? = null

    constructor(port: Int, hostname: String) {
        this.port = port
        this.hostname = hostname
        this.serverConnection = this
    }

    suspend fun initializeConnection() {
        try {

            client.ws(
                method = HttpMethod.Get,
                host = hostname,
                port = port,
                path = "/camera/createconnection"
            ) {
                // Set the websocket session context to be available.
                webSocketSession = this

                // Send the initial login message
                serverConnection!!.sendMessage( Message(
                    type = MessageType.LOGIN,
                    id = 1,
                    password = "secret"
                ))

                try {

                    // Continuously read incoming frames
                    for (frame in incoming) {
                        if (frame is Frame.Binary) {
                            val json = String(frame.readBytes())
                            val serverMessage = Json.decodeFromString<Message>(json)
                            MessageHandler.handleMessage(serverMessage, serverConnection!!)
                        }
                    }

                } catch (ex: ClosedChannelException) {
                    Log.e("taggg", "exception: channel closed!")
                }

                webSocketSession = null
            }

            client.close()

        } catch (ex: ClosedSendChannelException) {
            Log.e(tag, ex.message.toString())
        } catch (ex: ConnectTimeoutException) {
            Log.e(tag, ex.message.toString())
        } catch (ex: UnresolvedAddressException) {
            Log.e(tag, ex.message.toString())
        } finally {
            webSocketSession = null
        }
    }

    suspend fun sendMessage(message: Message) {
        if (webSocketSession == null) {
            // TODO: Log
            return
        }

        try {
            // Encode message to JSON
            val dataBa = Json.encodeToString(message).toByteArray()

            // Create bytearray which contains the message size
            val sizeBa = ByteArray(Int.SIZE_BYTES) {
                (dataBa.size shr (it * 8)).toByte()
            }

            // Combine the two bytearrays and send them
            val totalBa = sizeBa + dataBa
            val totalFrame = Frame.byType(true, FrameType.BINARY, totalBa)

            webSocketSession!!.outgoing.send(totalFrame)
        } catch (ex: ClosedChannelException) {
            webSocketSession = null
        }
    }

    fun isConnected() = webSocketSession != null

}
