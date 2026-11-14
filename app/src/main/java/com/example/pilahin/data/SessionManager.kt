package com.example.pilahin.data

import android.content.Context

/** Menyimpan sesi (token dari backend) dan profil di HP. */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_session", Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = prefs.getBoolean("logged_in", false)
    val name: String get() = prefs.getString("name", "Pengguna") ?: "Pengguna"
    val username: String
        get() = (prefs.getString("username", "pengguna") ?: "pengguna").removePrefix("@")
    val email: String get() = prefs.getString("email", "") ?: ""
    val photoPath: String? get() = prefs.getString("photo", null)
    val phone: String get() = prefs.getString("phone", "") ?: ""

    /** Token login dari backend. null = belum login lewat server. */
    val token: String? get() = prefs.getString("token", null)

    /** Dipanggil setelah /api/login berhasil. */
    fun saveLogin(token: String, user: ApiUser) {
        prefs.edit()
            .putBoolean("logged_in", true)
            .putString("token", token)
            .apply()
        saveUser(user)
    }

    /** Simpan data akun terbaru dari server, termasuk alamat foto profil. */
    fun saveUser(user: ApiUser) {
        prefs.edit()
            .putLong("user_id", user.id)
            .putString("name", user.fullName)
            .putString("username", user.username)
            .putString("email", user.email)
            .putString("phone", user.phone)
            .putString("photo", user.photoUrl)
            .apply()
    }

    fun login(name: String, username: String, email: String = "") {
        prefs.edit()
            .putBoolean("logged_in", true)
            .putString("name", name)
            .putString("username", username)
            .putString("email", email)
            .apply()
    }

    fun updateProfile(name: String, username: String, email: String, photoPath: String?) {
        prefs.edit()
            .putString("name", name)
            .putString("username", username)
            .putString("email", email)
            .putString("photo", photoPath)
            .apply()
    }

    /** Simulasi: hash disimpan lokal. Nanti diganti permintaan ubah password ke backend. */
    fun setPasswordHash(hash: String) {
        prefs.edit().putString("password_hash", hash).apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}