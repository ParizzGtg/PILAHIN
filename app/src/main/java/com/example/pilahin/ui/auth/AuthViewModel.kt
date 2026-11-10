package com.example.pilahin.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.AuthRepository
import com.example.pilahin.data.FakeAuthRepository
import kotlinx.coroutines.launch

enum class GoogleAuthResult { SUCCESS, NOT_REGISTERED, ALREADY_REGISTERED }

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Nanti ganti dengan implementasi backend asli
    private val repository: AuthRepository = FakeAuthRepository(app)

    fun googleLogin(email: String, onResult: (GoogleAuthResult) -> Unit) {
        viewModelScope.launch {
            val result = if (repository.isEmailRegistered(email))
                GoogleAuthResult.SUCCESS else GoogleAuthResult.NOT_REGISTERED
            onResult(result)
        }
    }

    fun googleRegister(email: String, onResult: (GoogleAuthResult) -> Unit) {
        viewModelScope.launch {
            if (repository.isEmailRegistered(email)) {
                onResult(GoogleAuthResult.ALREADY_REGISTERED)
            } else {
                repository.registerEmail(email)
                onResult(GoogleAuthResult.SUCCESS)
            }
        }
    }

    /** true = berhasil didaftarkan, false = email sudah terdaftar */
    fun registerManual(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (repository.isEmailRegistered(email)) {
                onResult(false)
            } else {
                repository.registerEmail(email)
                onResult(true)
            }
        }
    }
}