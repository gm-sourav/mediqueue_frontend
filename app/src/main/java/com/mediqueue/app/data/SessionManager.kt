package com.mediqueue.app.data

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("mediqueue_session", Context.MODE_PRIVATE)

    fun save(auth: AuthResponse) {
        prefs.edit()
            .putString("token", auth.token)
            .putLong("id", auth.id)
            .putString("name", auth.name)
            .putString("email", auth.email)
            .putString("role", auth.role)
            .apply()
    }

    val token: String? get() = prefs.getString("token", null)
    val role: String? get() = prefs.getString("role", null)
    val name: String get() = prefs.getString("name", "") ?: ""
    val email: String get() = prefs.getString("email", "") ?: ""
    val userId: Long get() = prefs.getLong("id", -1L)

    val isLoggedIn: Boolean get() = !token.isNullOrBlank() && !role.isNullOrBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }
}