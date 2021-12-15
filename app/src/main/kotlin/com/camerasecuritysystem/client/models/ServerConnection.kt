package com.camerasecuritysystem.client.models

import android.content.Context
import android.util.Log
import com.camerasecuritysystem.client.KeyStoreHelper
import com.camerasecuritysystem.client.R
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

    companion object {
        @Volatile
        private var INSTANCE: ServerConnection? = null

        fun getInstance(): ServerConnection {
            if (INSTANCE == null) {
                INSTANCE = ServerConnection()
            }
            return INSTANCE!!
        }
    }

    private val tag: String = "initializeConnection"

    private var client: HttpClient = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 100 * 1000
        }
    }

    private var webSocketSession: DefaultClientWebSocketSession? = null

    private suspend fun initializeConnection(credentials: HashMap<String, String>) {
        try {

            val cameraId: Int? = credentials["camera_id"]?.toInt()
            val pwd = credentials["encPwd"]?.toByteArray(Charsets.ISO_8859_1)

            val pwdIVByte = credentials["pwdIVByte"]?.toByteArray(Charsets.ISO_8859_1)
            val port: Int? = credentials["port"]?.toInt()
            val hostname = credentials["ip_address"]

            client.ws(
                method = HttpMethod.Get,
                host = hostname!!,
                port = port!!,
                path = "/camera/createconnection"
            ) {
                // Set the websocket session context to be available.
                webSocketSession = this

                // Send the initial login message
                getInstance().sendMessage(
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
                            MessageHandler.handleMessage(serverMessage, getInstance()!!)
                        }
                    }
                } catch (ex: ClosedChannelException) {
                    Log.e("Channel", "exception: channel closed!")
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

    private fun credentialsEntered(context: Context): Boolean {
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

        return credentials.contains(null) == false
    }

    suspend fun connectIfPossible(context: Context) {
        if (credentialsEntered(context)) {
            initializeConnection(getCredentials(context))
        }
        Log.e("Creds entered", "${credentialsEntered(context)}")
    }

    private fun getCredentials(context: Context): HashMap<String, String> {
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

        val hashMap: HashMap<String, String> = HashMap()

        hashMap.put(context.resources.getString(R.string.camera_id), camera_id!!)
        hashMap.put(context.resources.getString(R.string.encPwd), pwd!!)
        hashMap.put(context.resources.getString(R.string.pwdIVByte), pwdIVByte!!)
        hashMap.put(context.resources.getString(R.string.port), port!!)
        hashMap.put(context.resources.getString(R.string.ip_address), hostname!!)

        return hashMap
    }
}
