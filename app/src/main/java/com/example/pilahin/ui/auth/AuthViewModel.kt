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
import com.example.pilahin.data.GoogleSignIn
import kotlinx.coroutines.launch

enum class GoogleMode { LOGIN, REGISTER }

sealed class GoogleResult {
    object Success : GoogleResult()
    object Cancelled : GoogleResult()                       // pengguna menutup pemilih akun
    data class NotRegistered(val message: String) : GoogleResult()
    data class Error(val message: String) : GoogleResult()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Akun lewat backend Flask (register, login, Google)
    private val account = AccountRepository(app)

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

    /**
     * Tampilkan pemilih akun Google, lalu kirim ID token ke server.
     * activityContext: LocalContext.current dari layar (harus Activity).
     */
    fun google(activityContext: Context, mode: GoogleMode, onResult: (GoogleResult) -> Unit) {
        if (isLoading) return
        isLoading = true
        viewModelScope.launch {
            val tokenResult = GoogleSignIn.getIdToken(activityContext)
            val result = tokenResult.fold(
                onSuccess = { idToken ->
                    val modeText = if (mode == GoogleMode.REGISTER) "register" else "login"
                    val auth = account.googleAuth(idToken, modeText)
                    val error = auth.exceptionOrNull()
                    when {
                        error == null -> GoogleResult.Success
                        error is ApiException && error.code == 404 ->
                            GoogleResult.NotRegistered(error.message ?: "Akun belum terdaftar")
                        else -> GoogleResult.Error(error.message ?: "Login Google gagal")
                    }
                },
                onFailure = { error ->
                    if (error is GoogleCancelledException) GoogleResult.Cancelled
                    else GoogleResult.Error(error.message ?: "Login Google gagal")
                }
            )
            isLoading = false
            onResult(result)
        }
    }

    /** Lupakan pilihan akun Google saat logout. */
    fun clearGoogleState() {
        viewModelScope.launch { GoogleSignIn.clearState(getApplication()) }
    }
}