package com.example.pilahin.data

/**
 * Kontrak data akun. Teman backend nanti membuat implementasi aslinya
 * (Retrofit / Firebase) tanpa mengubah UI.
 */
interface AuthRepository {
    suspend fun isEmailRegistered(email: String): Boolean
    suspend fun registerEmail(email: String)
}