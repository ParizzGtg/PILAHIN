package com.example.pilahin.data

import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Pengirim permintaan ke backend Flask di VPS.
 * Memakai HttpURLConnection bawaan Android, jadi tidak perlu library tambahan.
 */
object ApiClient {

    // GANTI dengan IP VPS kamu (tanpa garis miring di akhir)
    const val BASE_URL = "http://43.157.225.187:5000"

    class Response(val code: Int, val body: JSONObject) {
        val isSuccess: Boolean
            get() = code in 200..299 && body.optBoolean("success", false)
        val message: String
            get() = body.optString("message", "Terjadi kesalahan ($code)")
    }

    /** Kirim data JSON (register, login, update profil, hapus akun). */
    suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        token: String? = null
    ): Response = send(
        method = method,
        path = path,
        token = token,
        contentType = if (body != null) "application/json; charset=utf-8" else null,
        writeBody = if (body != null) {
            { out -> out.write(body.toString().toByteArray(Charsets.UTF_8)) }
        } else {
            null
        }
    )

    /** Kirim satu file gambar JPEG (multipart/form-data), dipakai untuk foto profil. */
    suspend fun uploadImage(
        path: String,
        fieldName: String,
        fileName: String,
        jpegBytes: ByteArray,
        token: String?
    ): Response {
        val boundary = "PilahinBoundary${System.currentTimeMillis()}"
        return send(
            method = "POST",
            path = path,
            token = token,
            contentType = "multipart/form-data; boundary=$boundary"
        ) { out ->
            val header = "--$boundary\r\n" +
                    "Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"\r\n" +
                    "Content-Type: image/jpeg\r\n\r\n"
            out.write(header.toByteArray(Charsets.UTF_8))
            out.write(jpegBytes)
            out.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8))
        }
    }

    private suspend fun send(
        method: String,
        path: String,
        token: String?,
        contentType: String?,
        writeBody: ((OutputStream) -> Unit)?
    ): Response = withContext(Dispatchers.IO) {
        try {
            val conn = URL(BASE_URL + path).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                conn.connectTimeout = 10_000
                conn.readTimeout = 30_000
                conn.setRequestProperty("Accept", "application/json")
                if (token != null) conn.setRequestProperty("Authorization", "Bearer $token")

                if (writeBody != null) {
                    conn.doOutput = true
                    if (contentType != null) conn.setRequestProperty("Content-Type", contentType)
                    conn.outputStream.use { writeBody(it) }
                }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = try {
                    JSONObject(text)
                } catch (e: Exception) {
                    JSONObject().put("success", false).put("message", "Respon server tidak valid ($code)")
                }
                Response(code, json)
            } finally {
                conn.disconnect()
            }
        } catch (e: IOException) {
            Response(
                0,
                JSONObject()
                    .put("success", false)
                    .put("message", "Tidak bisa terhubung ke server. Periksa koneksi internet.")
            )
        }
    }
}