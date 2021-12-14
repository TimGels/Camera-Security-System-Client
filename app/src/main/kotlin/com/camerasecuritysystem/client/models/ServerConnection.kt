package com.camerasecuritysystem.client.models

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.camerasecuritysystem.client.KeyStoreHelper
import com.camerasecuritysystem.client.R
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*

import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.nio.channels.ClosedChannelException
import java.util.logging.Handler

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
    private lateinit var context: Context
    private lateinit var activity: Activity

    constructor(port: Int, hostname: String) {
        this.port = port
        this.hostname = hostname
        this.serverConnection = this
    }

    constructor(activity: Activity) {
        this.activity = activity
        this.context = activity.applicationContext
        this.serverConnection = this
    }

    private suspend fun initializeConnection() {
        try {
            var sharedPreferences =
                context.getSharedPreferences(
                    "com.camerasecuritysystem.client",
                    Context.MODE_PRIVATE
                )

            val cameraId =
                sharedPreferences.getString(context.resources.getString(R.string.camera_id), null)
                    ?.toInt()
            val pwd =
                sharedPreferences.getString(context.resources.getString(R.string.encPwd), null)
                    ?.toByteArray(Charsets.ISO_8859_1)
            val pwdIVByte =
                sharedPreferences.getString(context.resources.getString(R.string.pwdIVByte), null)
                    ?.toByteArray(Charsets.ISO_8859_1)
            val port = sharedPreferences.getString(context.resources.getString(R.string.port), "0")
                ?.toInt()
            val hostname =
                sharedPreferences!!.getString(
                    context.resources.getString(R.string.ip_address),
                    null
                )

            client.ws(
                method = HttpMethod.Get,
                host = hostname!!,
                port = port!!,
                path = "/camera/createconnection"
            ) {
                // Set the websocket session context to be available.
                webSocketSession = this

                // Send the initial login message
                serverConnection!!.sendMessage(
                    Message(
                        type = MessageType.LOGIN,
                        id = cameraId!!,
                        password = KeyStoreHelper("connectToServer").decryptData(pwdIVByte!!, pwd!!)
                    )
                )

                try {

                    // Continuously read incoming frames
                    for (frame in incoming) {
                        if (frame is Frame.Binary) {
                            val json = String(frame.readBytes())
                            val serverMessage: Message =
                                Json.decodeFromString(json) //decodeFromString(json)
                            MessageHandler.handleMessage(serverMessage, serverConnection!!)
                        }
                    }

                    Log.e("WEBSOCKET", "$webSocketSession")


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

            activity.runOnUiThread {
                Toast.makeText(activity, "Connection refused", Toast.LENGTH_SHORT).show()
            }

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

    private fun credentialsEntered(): Boolean {
        var sharedPreferences =
            context.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)


        val camera_id =
            sharedPreferences.getString(context.resources.getString(R.string.camera_id), null)
        val pwd = sharedPreferences.getString(context.resources.getString(R.string.encPwd), null)
        val pwdIVByte =
            sharedPreferences.getString(context.resources.getString(R.string.pwdIVByte), null)
        val port = sharedPreferences.getString(context.resources.getString(R.string.port), null)
        val hostname =
            sharedPreferences.getString(context.resources.getString(R.string.ip_address), null)

        var credentials = arrayOf(camera_id, pwd, pwdIVByte, port, hostname)
        if (credentials.contains(null)) {
            return false
        }
        return true

    }

    suspend fun connectIfPossible() {
        if (credentialsEntered()) {
            initializeConnection()
        }
        Log.e("Creds enetered", "${credentialsEntered()}")
    }
}
