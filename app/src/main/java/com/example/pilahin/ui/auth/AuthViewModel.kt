package com.example.pilahin.ui.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.AccountRepository
import com.example.pilahin.data.AuthRepository
import com.example.pilahin.data.FakeAuthRepository
import kotlinx.coroutines.launch

enum class GoogleAuthResult { SUCCESS, NOT_REGISTERED, ALREADY_REGISTERED }

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Akun lewat backend Flask (register, login)
    private val account = AccountRepository(app)

    // SEMENTARA: login/daftar Google masih simulasi di HP, belum ke backend
    private val repository: AuthRepository = FakeAuthRepository(app)

    /** true selama menunggu jawaban server, untuk tombol "Memproses..." */
    var isLoading by mutableStateOf(false)
        private set

    /** onResult(null) = berhasil, onResult("pesan") = gagal */
    fun login(identifier: String, password: String, onResult: (error: String?) -> Unit) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val result = account.login(identifier, password)
            isLoading = false
            onResult(result.exceptionOrNull()?.message)
        }
    }

    /** Daftar ke server, lalu langsung login agar dapat token. */
    fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        onResult: (error: String?) -> Unit
    ) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val registered = account.register(username, fullName, email, phone, password)
            val result = if (registered.isSuccess) {
                account.login(email, password).map { }
            } else {
                registered
            }
            isLoading = false
            onResult(result.exceptionOrNull()?.message)
        }
    }

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
}