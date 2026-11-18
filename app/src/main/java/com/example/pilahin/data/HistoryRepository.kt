package com.example.pilahin.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class HistoryRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_history", Context.MODE_PRIVATE)

    fun getAll(): List<ScanResult> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                ScanResult(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    category = WasteCategory.valueOf(o.getString("category")),
                    confidence = o.getDouble("confidence").toFloat(),
                    imagePath = o.getString("imagePath"),
                    timeMillis = o.getLong("time"),
                    reason = o.optString("reason").ifBlank { null },
                    disposal = o.optJSONArray("disposal")?.let { a ->
                        (0 until a.length()).map { i -> a.getString(i) }
                    } ?: emptyList(),
                    uncertain = o.optBoolean("uncertain", false)
                )
            }.sortedByDescending { it.timeMillis }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(item: ScanResult) {
        val arr = JSONArray()
        (getAll().filter { it.id != item.id } + item).forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("category", it.category.name)
                    .put("confidence", it.confidence.toDouble())
                    .put("imagePath", it.imagePath)
                    .put("time", it.timeMillis)
                    .put("reason", it.reason ?: "")
                    .put("disposal", JSONArray(it.disposal))
                    .put("uncertain", it.uncertain)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object {
        const val KEY = "items"
    }
}