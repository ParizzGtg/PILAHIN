package com.example.pilahin.data

import android.content.Context

/** Versi sementara: menyimpan email terdaftar di HP (SharedPreferences). */
class FakeAuthRepository(context: Context) : AuthRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_fake_auth", Context.MODE_PRIVATE)

    private fun load(): Set<String> =
        prefs.getStringSet(KEY, emptySet()) ?: emptySet()

    private fun normalize(email: String) = email.trim().lowercase()

    override suspend fun isEmailRegistered(email: String): Boolean =
        load().contains(normalize(email))

    override suspend fun registerEmail(email: String) {
        prefs.edit().putStringSet(KEY, load() + normalize(email)).apply()
    }

    private companion object {
        const val KEY = "registered_emails"
    }
}