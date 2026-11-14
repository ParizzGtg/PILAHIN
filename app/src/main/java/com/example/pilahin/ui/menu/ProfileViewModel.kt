package com.example.pilahin.ui.menu

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.SessionManager
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserProfile(
    val name: String,
    val username: String,
    val email: String,
    val photoPath: String?
)

class ProfileViewModel(private val app: Application) : AndroidViewModel(app) {

    // Nanti ganti dengan implementasi backend asli
    private val session = SessionManager(app)

    private val _profile = MutableStateFlow(read())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private fun read() = UserProfile(session.name, session.username, session.email, session.photoPath)

    /** Salin foto pilihan ke penyimpanan sementara agar akses tidak hilang. */
    fun stagePhoto(uri: Uri, onReady: (String) -> Unit) {
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(app.cacheDir, "profile_stage_${System.currentTimeMillis()}.jpg")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { input.copyTo(it) }
                    }
                    file.absolutePath
                }.getOrNull()
            }
            if (path != null) onReady(path)
        }
    }

    fun save(
        name: String,
        username: String,
        email: String,
        newPassword: String?,
        stagedPhoto: String?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            var photo = session.photoPath
            if (stagedPhoto != null) {
                photo = withContext(Dispatchers.IO) {
                    runCatching {
                        val dest = File(app.filesDir, "profile_${System.currentTimeMillis()}.jpg")
                        File(stagedPhoto).copyTo(dest, overwrite = true)
                        session.photoPath?.let { File(it).delete() }
                        dest.absolutePath
                    }.getOrDefault(photo)
                }
            }
            session.updateProfile(name.trim(), username.trim(), email.trim(), photo)
            if (!newPassword.isNullOrEmpty()) session.setPasswordHash(sha256(newPassword))
            _profile.value = read()
            onDone()
        }
    }

    /**
     * Hapus akun: bersihkan semua data lokal milik pengguna lalu akhiri sesi.
     * Nanti tambahkan pemanggilan DELETE ke backend di sini.
     */
    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            val email = session.email.trim().lowercase()
            withContext(Dispatchers.IO) {
                listOf("pilahin_history", "pilahin_pickup", "pilahin_notifications").forEach {
                    app.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().apply()
                }
                if (email.isNotBlank()) {
                    val auth = app.getSharedPreferences("pilahin_fake_auth", Context.MODE_PRIVATE)
                    val emails = auth.getStringSet("registered_emails", emptySet()) ?: emptySet()
                    auth.edit().putStringSet("registered_emails", emails - email).apply()
                }
                File(app.filesDir, "history").deleteRecursively()
                app.filesDir.listFiles()
                    ?.filter { it.name.startsWith("profile_") }
                    ?.forEach { it.delete() }
            }
            session.logout()
            Toast.makeText(app, "Akun berhasil dihapus", Toast.LENGTH_LONG).show()
            onDone()
        }
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray())
            .joinToString("") { "%02x".format(it) }
}