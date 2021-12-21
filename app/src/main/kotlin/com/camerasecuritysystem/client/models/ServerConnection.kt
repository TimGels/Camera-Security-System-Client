package com.camerasecuritysystem.client.models

import android.content.Context
import android.util.Log
import com.camerasecuritysystem.client.CSSApplication
import com.camerasecuritysystem.client.KeyStoreHelper
import com.camerasecuritysystem.client.R
import com.camerasecuritysystem.client.cassert
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.FrameType
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.close
import io.ktor.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.channels.ClosedChannelException
import java.nio.channels.UnresolvedAddressException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.net.ConnectException
import java.net.NoRouteToHostException

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

    object Constants {
        const val connectTimeout: Long = 30 * 1000
    }

    private val tag: String = "initializeConnection"

    private var client: HttpClient = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = Constants.connectTimeout
        }
    }

    private var webSocketSession: DefaultClientWebSocketSession? = null

    private var _state: ConnectionState = ConnectionState.CLOSED
    private var state: ConnectionState
        get() = _state
        set(value) {
            _state = value
            // Invoke callback on every state change.
            _listener?.invoke(_state)
        }

    private var _listener: ((state: ConnectionState) -> Unit)? = null

    private suspend fun initializeConnection(credentials: HashMap<String, String>) {

        val hostname = credentials["ip_address"]
        val port: Int? = credentials["port"]?.toInt()
        val cameraId: Int? = credentials["camera_id"]?.toInt()
        val pwdIVByte = credentials["pwdIVByte"]?.toByteArray(Charsets.ISO_8859_1)
        val pwd = credentials["encPwd"]?.toByteArray(Charsets.ISO_8859_1)

        cassert(webSocketSession == null)
        cassert(state == ConnectionState.CLOSED)

        try {
            state = ConnectionState.CONNECTING

            client.ws(
                method = HttpMethod.Get,
                host = hostname!!,
                port = port!!,
                path = "/camera/createconnection"
            ) {
                // Set the websocket session context to be available.
                webSocketSession = this
                state = ConnectionState.CONNECTED

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
                            val serverMessage: Message = Json.decodeFromString(json)
                            MessageHandler.handleMessage(serverMessage, getInstance())
                        }
                    }
                } catch (ex: ClosedChannelException) {
                    Log.e("Channel closed", "${ex.message}")
                } catch (ex: Exception) {
                    Log.e(tag, "Error occured while receiving: ${ex.message}")
                } finally {
                    webSocketSession?.close()
                    webSocketSession = null
                }
            }

            Log.d(tag, "go-away")

            // TODO: Show toast on relevant error.
        } catch (ex: ConnectTimeoutException) {
            Log.e(tag, "ConnectTimeout: ${ex.message}")
        } catch (ex: UnresolvedAddressException) {
            Log.e(tag, "UnresolvedAddress: ${ex.message}")
        } catch (ex: NoRouteToHostException) {
            Log.e(tag, "NoRouteToHost: ${ex.message}")
        } catch (ex: ConnectException) {
            Log.e(tag, "Connection error: ${ex.message}")
        } catch (ex: Exception) {
            Log.e(tag, "Error occured: ${ex.message}")
        } finally {
            state = ConnectionState.CLOSED
        }

        cassert(webSocketSession == null)
        cassert(state == ConnectionState.CLOSED)
    }

    suspend fun sendMessage(message: Message) {
        if (webSocketSession == null) {
            Log.e(tag, "send called with null session!")
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
            Log.e(tag, "Error while sending: ${ex.message}")
            webSocketSession = null
        }
    }

    fun close() {
        if (!isConnected()) {
            Log.e(tag, "Not closing while not connected!")
            return
        }

        cassert(webSocketSession != null)
        cassert(state == ConnectionState.CONNECTED)

        GlobalScope.launch {
            webSocketSession!!.close()
        }
    }

    fun isConnected() = webSocketSession != null && state == ConnectionState.CONNECTED

    fun addStateCallback(callback: ((state: ConnectionState) -> Unit)) {
        _listener = callback
        // Invoke the callback once.
        callback(_state)
    }

    private fun credentialsEntered(): Boolean {
        val context = CSSApplication.context

        val sharedPreferences =
            context.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)

        val cameraid =
            sharedPreferences.getString(context.resources.getString(R.string.camera_id), null)
        val pwd = sharedPreferences.getString(context.resources.getString(R.string.encPwd), null)
        val pwdIVByte =
            sharedPreferences.getString(context.resources.getString(R.string.pwdIVByte), null)
        val port = sharedPreferences.getString(context.resources.getString(R.string.port), null)
        val hostname =
            sharedPreferences.getString(context.resources.getString(R.string.ip_address), null)

        val credentials = arrayOf(cameraid, pwd, pwdIVByte, port, hostname)

        return credentials.contains(null) == false
    }

    fun connectIfPossible() {
        if (isConnected()) {
            Log.e(tag, "Already connected")
            return
        }

        if (state == ConnectionState.CONNECTING) {
            Log.e(tag, "Won't connect when connecting!")
            return
        }

        if (!credentialsEntered()) {
            state = ConnectionState.NO_CREDENTIALS
            return
        }

        // If we are here, the user has successfully entered credentials.
        if (state == ConnectionState.NO_CREDENTIALS) {
            _state = ConnectionState.CLOSED // Don't invoke callback
        }

        GlobalScope.launch {
            initializeConnection(getCredentials())
        }
    }

    private fun getCredentials(): HashMap<String, String> {
        val context = CSSApplication.context

        val sharedPreferences =
            context.getSharedPreferences("com.camerasecuritysystem.client", Context.MODE_PRIVATE)

        val cameraid =
            sharedPreferences.getString(context.resources.getString(R.string.camera_id), null)
        val pwd = sharedPreferences.getString(context.resources.getString(R.string.encPwd), null)
        val pwdIVByte =
            sharedPreferences.getString(context.resources.getString(R.string.pwdIVByte), null)
        val port = sharedPreferences.getString(context.resources.getString(R.string.port), null)
        val hostname =
            sharedPreferences.getString(context.resources.getString(R.string.ip_address), null)

        val hashMap: HashMap<String, String> = HashMap()

        hashMap.put(context.resources.getString(R.string.camera_id), cameraid!!)
        hashMap.put(context.resources.getString(R.string.encPwd), pwd!!)
        hashMap.put(context.resources.getString(R.string.pwdIVByte), pwdIVByte!!)
        hashMap.put(context.resources.getString(R.string.port), port!!)
        hashMap.put(context.resources.getString(R.string.ip_address), hostname!!)

        return hashMap
    }
}
