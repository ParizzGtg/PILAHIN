package com.example.pilahin.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Detection(
    val label: String,
    val category: WasteCategory,
    val confidence: Float,
    val left: Float,   // semua koordinat 0..1 terhadap frame yang dikirim
    val top: Float,
    val right: Float,
    val bottom: Float
)

data class DetectionFrame(val items: List<Detection>, val width: Int, val height: Int)

class DetectionRepository(context: Context) {

    private val session = SessionManager(context.applicationContext)

    suspend fun detect(jpeg: ByteArray, width: Int, height: Int): Result<List<Detection>> {
        val token = session.token ?: return Result.failure(ApiException("Sesi berakhir", 401))
        val res = ApiClient.uploadImage("/api/detect", "image", "frame.jpg", jpeg, token)
        if (!res.isSuccess) return Result.failure(ApiException(res.message, res.code))
        val arr = res.body.optJSONArray("detections")
            ?: res.body.optJSONArray("objects")
            ?: res.body.optJSONObject("result")?.optJSONArray("detections")
        return Result.success(parse(arr, width, height))
    }

    private fun parse(arr: JSONArray?, w: Int, h: Int): List<Detection> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            runCatching { one(arr.getJSONObject(i), w, h) }.getOrNull()
        }
    }

    private fun one(o: JSONObject, w: Int, h: Int): Detection? {
        val box = o.optJSONArray("box") ?: o.optJSONArray("bbox") ?: return null
        if (box.length() < 4) return null
        var v = (0 until 4).map { box.optDouble(it).toFloat() }
        // Kalau server mengirim piksel, ubah jadi 0..1
        if (v.any { it > 1.5f }) v = listOf(v[0] / w, v[1] / h, v[2] / w, v[3] / h)
        val category = runCatching { WasteCategory.valueOf(o.getString("category").uppercase()) }
            .getOrDefault(WasteCategory.RESIDU)
        return Detection(
            label = o.optString("label").ifBlank { o.optString("name", category.label) },
            category = category,
            confidence = o.optDouble("confidence", 0.0).toFloat(),
            left = v[0].coerceIn(0f, 1f),
            top = v[1].coerceIn(0f, 1f),
            right = v[2].coerceIn(0f, 1f),
            bottom = v[3].coerceIn(0f, 1f)
        )
    }
}