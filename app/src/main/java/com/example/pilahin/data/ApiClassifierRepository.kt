package com.example.pilahin.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Klasifikasi sungguhan: foto dikirim ke server (POST /api/scan),
 * lalu model AI PILAHIN di server yang menebak jenis & kategorinya.
 * Foto TIDAK disimpan di server, hanya dipakai untuk prediksi.
 */
class ApiClassifierRepository(context: Context) : ClassifierRepository {

    private val session = SessionManager(context.applicationContext)

    override suspend fun classify(image: File): ScanResult {
        val token = session.token
            ?: throw ApiException("Sesi berakhir, silakan login ulang", 401)

        // 1. Kecilkan foto dulu agar upload cepat (sekitar 100-250 KB)
        val bytes = withContext(Dispatchers.Default) { compress(image.absolutePath) }

        // 2. Kirim ke server
        val res = ApiClient.uploadImage(
            path = "/api/scan",
            fieldName = "image",
            fileName = "scan.jpg",
            jpegBytes = bytes,
            token = token
        )
        if (!res.isSuccess) throw ApiException(res.message, res.code)

        // 3. Ubah jawaban server jadi ScanResult
        val r = res.body.getJSONObject("result")
        val category = runCatching { WasteCategory.valueOf(r.getString("category")) }
            .getOrDefault(WasteCategory.RESIDU)
        val steps = r.optJSONArray("disposal")
        return ScanResult(
            id = UUID.randomUUID().toString(),
            name = r.optString("name", category.label),
            category = category,
            confidence = r.optDouble("confidence", 0.0).toFloat(),
            imagePath = image.absolutePath,
            timeMillis = System.currentTimeMillis(),
            reason = r.optString("reason").ifBlank { null },
            disposal = if (steps == null) emptyList() else (0 until steps.length()).map { steps.getString(it) },
            uncertain = r.optBoolean("uncertain", false)
        )
    }

    /**
     * Foto diperkecil (sisi terpanjang maks 1024 px), diputar sesuai EXIF,
     * lalu disimpan ulang sebagai JPEG kualitas 85. Foto TIDAK dipotong
     * karena server sendiri yang mencari objek sampahnya.
     */
    private fun compress(path: String, maxSide: Int = 1024): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Foto tidak bisa dibaca" }

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSide) sample *= 2
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

        val longest = maxOf(decoded.width, decoded.height)
        val scale = if (longest > maxSide) maxSide.toFloat() / longest else 1f
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postRotate(rotation)
        }
        val ready = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        val out = ByteArrayOutputStream()
        ready.compress(Bitmap.CompressFormat.JPEG, 85, out)
        if (ready !== decoded) ready.recycle()
        decoded.recycle()
        return out.toByteArray()
    }
}