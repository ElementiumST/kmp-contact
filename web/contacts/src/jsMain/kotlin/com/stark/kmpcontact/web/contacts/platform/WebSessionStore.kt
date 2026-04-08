package com.stark.kmpcontact.web.contacts.platform

import kotlinx.browser.window

class WebSessionStore {
    fun getSessionId(): String? = window.sessionStorage.getItem(KEY_SESSION_ID)

    fun saveSessionId(sessionId: String) {
        window.sessionStorage.setItem(KEY_SESSION_ID, sessionId)
    }

    fun clear() {
        window.sessionStorage.removeItem(KEY_SESSION_ID)
    }

    private companion object {
        const val KEY_SESSION_ID = "session_id"
    }
}
