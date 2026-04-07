package com.stark.kmpcontact.android.contacts.data.auth

import android.content.SharedPreferences

class AuthSessionStore(
    private val sharedPreferences: SharedPreferences,
) {
    @Synchronized
    fun getSessionId(): String? = sharedPreferences.getString(KEY_SESSION_ID, null)

    @Synchronized
    fun saveSessionId(sessionId: String) {
        sharedPreferences.edit().putString(KEY_SESSION_ID, sessionId).apply()
    }

    @Synchronized
    fun clear() {
        sharedPreferences.edit().remove(KEY_SESSION_ID).apply()
    }

    private companion object {
        const val KEY_SESSION_ID = "session_id"
    }
}
