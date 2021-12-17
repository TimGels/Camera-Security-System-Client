package com.camerasecuritysystem.client

import androidx.lifecycle.LiveData
import com.camerasecuritysystem.client.models.ConnectionState
import com.camerasecuritysystem.client.models.ServerConnection

class ServerLiveData : LiveData<ConnectionState>() {

    private val serverConnection = ServerConnection.getInstance()
    private val listener = { state: ConnectionState ->
        postValue(state)
    }

    override fun onActive() {
        serverConnection.addStateCallback(listener)
    }
}
