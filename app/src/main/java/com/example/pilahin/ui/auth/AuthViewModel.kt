package com.example.pilahin.ui.auth

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.AccountRepository
import com.example.pilahin.data.ApiException
import com.example.pilahin.data.GoogleCancelledException
import com.example.pilahin.data.GooglePasswordNeededException
import com.example.pilahin.data.GoogleSignIn
import kotlinx.coroutines.launch

enum class GoogleMode { LOGIN, REGISTER }

sealed class AuthResult {
    object Success : AuthResult()
    object Cancelled : AuthResult()                         // pengguna menutup pemilih akun
    data class NotRegistered(val message: String) : AuthResult()
    data class AlreadyRegistered(val message: String) : AuthResult()
    /** Daftar Google akun baru: pengguna harus membuat sandi PILAHIN dulu. */
    data class NeedGooglePassword(
        val idToken: String,
        val email: String,
        val fullName: String
    ) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

private fun Throwable.toAuthResult(): AuthResult = when {
    this is GoogleCancelledException -> AuthResult.Cancelled
    this is ApiException && code == 404 ->
        AuthResult.NotRegistered(message ?: "Akun belum terdaftar")
    this is ApiException &&
            (code == 409 || message.orEmpty().contains("sudah terdaftar", ignoreCase = true)) ->
        AuthResult.AlreadyRegistered(message ?: "Akun sudah terdaftar")
    else -> AuthResult.Error(message ?: "Terjadi kesalahan")
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Akun lewat backend Flask (register, login, Google)
    private val account = AccountRepository(app)

    /** true selama menunggu jawaban server, untuk tombol "Memproses..." */
    var isLoading by mutableStateOf(false)
        private set

    fun login(identifier: String, password: String, onResult: (AuthResult) -> Unit) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val result = account.login(identifier, password)
            isLoading = false
            onResult(result.fold(onSuccess = { AuthResult.Success }, onFailure = { it.toAuthResult() }))
        }
    }

    /** Hanya mendaftarkan akun. Tidak login otomatis. */
    fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String,
        onResult: (AuthResult) -> Unit
    ) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val result = account.register(username, fullName, email, phone, password)
            isLoading = false
            onResult(result.fold(onSuccess = { AuthResult.Success }, onFailure = { it.toAuthResult() }))
        }
    }

    /**
     * Tampilkan pemilih akun Google, lalu kirim ID token ke server.
     * activityContext: LocalContext.current dari layar (harus Activity).
     */
    fun google(activityContext: Context, mode: GoogleMode, onResult: (AuthResult) -> Unit) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val tokenResult = GoogleSignIn.getIdToken(activityContext)
            val result = tokenResult.fold(
                onSuccess = { idToken ->
                    val modeText = if (mode == GoogleMode.REGISTER) "register" else "login"
                    account.googleAuth(idToken, modeText).fold(
                        onSuccess = { AuthResult.Success },
                        onFailure = {
                            if (it is GooglePasswordNeededException)
                                AuthResult.NeedGooglePassword(idToken, it.email, it.fullName)
                            else it.toAuthResult()
                        }
                    )
                },
                onFailure = { it.toAuthResult() }
            )
            isLoading = false
            onResult(result)
        }
    }

    /** Daftar Google akun baru, sekalian dengan sandi PILAHIN yang dibuat pengguna. */
    fun googleRegisterWithPassword(idToken: String, password: String, onResult: (AuthResult) -> Unit) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val result = account.googleAuth(idToken, "register", password)
            isLoading = false
            onResult(result.fold(onSuccess = { AuthResult.Success }, onFailure = { it.toAuthResult() }))
        }
    }

    /** Lupakan pilihan akun Google saat logout. */
    fun clearGoogleState() {
        viewModelScope.launch { GoogleSignIn.clearState(getApplication()) }
    }
}