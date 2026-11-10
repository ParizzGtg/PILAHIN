package com.example.pilahin.data

import android.content.Context

/** Menyimpan status login di HP. Nanti bisa diganti token dari backend. */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_session", Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.getBoolean("logged_in", false)
    val name: String get() = prefs.getString("name", "Pengguna") ?: "Pengguna"
    val username: String get() = prefs.getString("username", "@pengguna") ?: "@pengguna"

    fun login(name: String, username: String) {
        prefs.edit()
            .putBoolean("logged_in", true)
            .putString("name", name)
            .putString("username", username)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}