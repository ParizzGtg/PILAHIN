package com.example.pilahin.data

import android.content.Context
import org.json.JSONObject

/** Data akun yang dikirim server (tanpa password). */
data class ApiUser(
    val id: Long,
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val photoUrl: String?
)

/** Error dari server, membawa pesan yang siap ditampilkan ke pengguna. */
class ApiException(message: String, val code: Int) : Exception(message)

/**
 * Register, login, update profil, dan hapus akun lewat backend Flask.
 * Token dan data akun disimpan otomatis ke SessionManager.
 */
class AccountRepository(context: Context) {

    private val session = SessionManager(context)

    suspend fun register(
        username: String,
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<Unit> {
        val body = JSONObject()
            .put("username", username)
            .put("full_name", fullName)
            .put("email", email)
            .put("phone", phone)
            .put("password", password)
        val res = ApiClient.request("POST", "/api/register", body)
        return if (res.isSuccess) Result.success(Unit) else fail(res)
    }

    suspend fun login(identifier: String, password: String): Result<ApiUser> {
        val body = JSONObject()
            .put("identifier", identifier)
            .put("password", password)
        val res = ApiClient.request("POST", "/api/login", body)
        if (!res.isSuccess) return fail(res)

        val user = parseUser(res.body.getJSONObject("user"))
        session.saveLogin(res.body.getString("token"), user)
        return Result.success(user)
    }

    suspend fun updateProfile(
        fullName: String,
        username: String,
        email: String,
        newPassword: String?
    ): Result<ApiUser> {
        val token = session.token ?: return notLoggedIn()
        val body = JSONObject()
            .put("full_name", fullName)
            .put("username", username)
            .put("email", email)
        if (!newPassword.isNullOrEmpty()) body.put("new_password", newPassword)

        val res = ApiClient.request("PUT", "/api/me", body, token)
        if (!res.isSuccess) return fail(res)

        val user = parseUser(res.body.getJSONObject("user"))
        session.saveUser(user)
        return Result.success(user)
    }

    /** Upload foto profil (sudah dikompres jadi JPEG). Foto lama otomatis dihapus server. */
    suspend fun uploadPhoto(jpegBytes: ByteArray): Result<ApiUser> {
        val token = session.token ?: return notLoggedIn()
        val res = ApiClient.uploadImage("/api/me/photo", "photo", "profile.jpg", jpegBytes, token)
        if (!res.isSuccess) return fail(res)

        val user = parseUser(res.body.getJSONObject("user"))
        session.saveUser(user)
        return Result.success(user)
    }

    suspend fun deleteAccount(): Result<Unit> {
        val token = session.token ?: return notLoggedIn()
        val res = ApiClient.request("DELETE", "/api/me?confirm=HAPUS", token = token)
        return if (res.isSuccess) Result.success(Unit) else fail(res)
    }

    private fun <T> fail(res: ApiClient.Response): Result<T> =
        Result.failure(ApiException(res.message, res.code))

    private fun <T> notLoggedIn(): Result<T> =
        Result.failure(ApiException("Sesi tidak valid, silakan login ulang", 401))

    private fun parseUser(o: JSONObject) = ApiUser(
        id = o.getLong("id"),
        username = o.getString("username"),
        fullName = o.getString("full_name"),
        email = o.getString("email"),
        phone = if (o.isNull("phone")) "" else o.getString("phone"),
        photoUrl = if (o.isNull("photo_url")) null else o.getString("photo_url")
    )
}