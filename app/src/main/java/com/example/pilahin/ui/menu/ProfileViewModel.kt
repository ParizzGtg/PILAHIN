package com.example.pilahin.ui.menu

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.AccountRepository
import com.example.pilahin.data.SessionManager
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Foto bisa berupa alamat server (http...) atau file di HP. Dipakai AsyncImage. */
fun photoModel(photo: String): Any =
    if (photo.startsWith("http")) photo else File(photo)

data class UserProfile(
    val name: String,
    val username: String,
    val email: String,
    val photoPath: String?
)

class ProfileViewModel(private val app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)
    private val account = AccountRepository(app)

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
        onDone: () -> Unit,
        onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            if (session.token != null) {
                saveToServer(name, username, email, newPassword, stagedPhoto, onDone, onError)
                return@launch
            }

            // SEMENTARA: akun Google simulasi (tanpa token) tetap simpan di HP
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

    private suspend fun saveToServer(
        name: String,
        username: String,
        email: String,
        newPassword: String?,
        stagedPhoto: String?,
        onDone: () -> Unit,
        onError: (message: String) -> Unit
    ) {
        // 1. Nama, username, email, password
        val result = account.updateProfile(name.trim(), username.trim(), email.trim(), newPassword)
        if (result.isFailure) {
            onError(result.exceptionOrNull()?.message ?: "Gagal menyimpan profil")
            return
        }

        // 2. Foto: kecilkan dulu, lalu upload. Server menghapus foto lama otomatis.
        if (stagedPhoto != null) {
            val oldLocalPhoto = session.photoPath?.takeIf { !it.startsWith("http") }
            val bytes = withContext(Dispatchers.IO) {
                runCatching { compressPhoto(stagedPhoto) }.getOrNull()
            }
            val upload = if (bytes != null) {
                account.uploadPhoto(bytes)
            } else {
                Result.failure(Exception("Foto tidak bisa dibaca"))
            }
            if (upload.isFailure) {
                _profile.value = read()
                val reason = upload.exceptionOrNull()?.message ?: "coba lagi"
                onError("Profil tersimpan, tapi foto gagal diunggah: $reason")
                return
            }
            withContext(Dispatchers.IO) {
                File(stagedPhoto).delete()
                oldLocalPhoto?.let { File(it).delete() } // sisa foto lokal versi lama
            }
        }

        _profile.value = read()
        onDone()
    }

    /**
     * Ubah foto jadi JPEG persegi 512x512, kualitas 80 (sekitar 50-100 KB).
     * Posisi foto diputar sesuai EXIF agar foto kamera tidak miring.
     */
    private fun compressPhoto(path: String, size: Int = 512): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Foto tidak bisa dibaca" }

        // Baca versi yang sudah diperkecil agar hemat memori
        var sample = 1
        while (minOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= size) sample *= 2
        val decoded = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: throw IllegalArgumentException("Foto tidak bisa dibaca")

        val rotation = runCatching {
            when (ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrDefault(0f)

        // Potong tengah jadi persegi, kecilkan, lalu putar
        val side = minOf(decoded.width, decoded.height)
        val scale = if (side > size) size.toFloat() / side else 1f
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation)
        }
        val square = Bitmap.createBitmap(
            decoded, (decoded.width - side) / 2, (decoded.height - side) / 2, side, side, matrix, true
        )

        val out = ByteArrayOutputStream()
        square.compress(Bitmap.CompressFormat.JPEG, 80, out)
        if (square !== decoded) square.recycle()
        decoded.recycle()
        return out.toByteArray()
    }

    /**
     * Hapus akun: hapus di server, lalu bersihkan semua data lokal dan akhiri sesi.
     */
    fun deleteAccount(onDone: () -> Unit) {
        viewModelScope.launch {
            if (session.token != null) {
                val result = account.deleteAccount()
                if (result.isFailure) {
                    val message = result.exceptionOrNull()?.message ?: "Gagal menghapus akun"
                    Toast.makeText(app, message, Toast.LENGTH_LONG).show()
                    return@launch
                }
            }

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