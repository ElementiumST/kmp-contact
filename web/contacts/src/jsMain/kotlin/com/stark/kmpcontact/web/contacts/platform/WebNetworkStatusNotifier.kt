package com.stark.kmpcontact.web.contacts.platform

class WebNetworkStatusNotifier {
    private val listeners = mutableListOf<() -> Unit>()

    fun addConnectionLostListener(listener: () -> Unit) {
        listeners += listener
    }

    fun notifyConnectionLost() {
        listeners.forEach { it() }
    }
}
