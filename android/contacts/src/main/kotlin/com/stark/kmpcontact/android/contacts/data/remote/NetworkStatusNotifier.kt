package com.stark.kmpcontact.android.contacts.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class NetworkStatusNotifier {
    private val _connectionLostEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val connectionLostEvents: SharedFlow<Unit> = _connectionLostEvents

    fun notifyConnectionLost() {
        _connectionLostEvents.tryEmit(Unit)
    }
}
