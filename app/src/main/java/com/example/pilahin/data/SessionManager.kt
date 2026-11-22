package com.example.pilahin.data

import android.content.Context

/** Menyimpan sesi (token dari backend) dan profil di HP. */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_session", Context.MODE_PRIVATE)

    // Terpisah dari sesi login, jadi tidak ikut terhapus saat logout / hapus akun
    private val appPrefs = context.applicationContext
        .getSharedPreferences("pilahin_app", Context.MODE_PRIVATE)

    val onboardingSeen: Boolean get() = appPrefs.getBoolean("onboarding_seen", false)

    fun markOnboardingSeen() {
        appPrefs.edit().putBoolean("onboarding_seen", true).apply()
    }

    val isLoggedIn: Boolean get() = prefs.getBoolean("logged_in", false)
    val name: String get() = prefs.getString("name", "Pengguna") ?: "Pengguna"
    val username: String
        get() = (prefs.getString("username", "pengguna") ?: "pengguna").removePrefix("@")
    val email: String get() = prefs.getString("email", "") ?: ""
    val photoPath: String? get() = prefs.getString("photo", null)
    val phone: String get() = prefs.getString("phone", "") ?: ""

    /** Hanya untuk memilih tampilan. Server tetap wajib memeriksa peran admin. */
    val isAdmin: Boolean get() = email.trim().equals(ADMIN_EMAIL, ignoreCase = true)

    /** Token login dari backend. null = belum login lewat server. */
    val token: String? get() = prefs.getString("token", null)

    /** Dipanggil setelah /api/login berhasil. */
    fun saveLogin(token: String, user: ApiUser) {
        markOnboardingSeen() // yang sudah login pasti sudah melewati onboarding
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
        markOnboardingSeen()
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

    /** Hapus sesi login saja. Penanda onboarding tetap tersimpan. */
    fun logout() {
        markOnboardingSeen() // jaga-jaga untuk sesi yang dibuat sebelum update
        prefs.edit().clear().apply()
    }

    companion object {
        const val ADMIN_EMAIL = "adminpilahin@gmail.com"
    }
}