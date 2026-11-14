package com.example.pilahin.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class PickupStatus(val label: String) {
    MENUNGGU("Menunggu konfirmasi"),
    DITERIMA("Diterima petugas"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan")
}

data class PickupRequest(
    val id: String,
    val address: String,
    val detail: String,
    val latitude: Double,
    val longitude: Double,
    val dayMillis: Long,
    val slot: String,
    val types: List<WasteCategory>,
    val note: String,
    val status: PickupStatus,
    val createdMillis: Long
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeMillis: Long,
    val read: Boolean
)

/** Nanti diganti implementasi backend (Retrofit). */
class PickupRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_pickup", Context.MODE_PRIVATE)

    fun getAll(): List<PickupRequest> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                PickupRequest(
                    id = o.getString("id"),
                    address = o.getString("address"),
                    detail = o.optString("detail"),
                    latitude = o.getDouble("lat"),
                    longitude = o.getDouble("lon"),
                    dayMillis = o.getLong("day"),
                    slot = o.getString("slot"),
                    types = o.optString("types").split(",")
                        .filter { s -> s.isNotBlank() }
                        .mapNotNull { s -> runCatching { WasteCategory.valueOf(s) }.getOrNull() },
                    note = o.optString("note"),
                    status = runCatching { PickupStatus.valueOf(o.getString("status")) }
                        .getOrDefault(PickupStatus.MENUNGGU),
                    createdMillis = o.getLong("created")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(list: List<PickupRequest>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("address", it.address)
                    .put("detail", it.detail)
                    .put("lat", it.latitude)
                    .put("lon", it.longitude)
                    .put("day", it.dayMillis)
                    .put("slot", it.slot)
                    .put("types", it.types.joinToString(",") { t -> t.name })
                    .put("note", it.note)
                    .put("status", it.status.name)
                    .put("created", it.createdMillis)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object {
        const val KEY = "requests"
    }
}

class NotificationRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_notifications", Context.MODE_PRIVATE)

    fun getAll(): List<AppNotification> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                AppNotification(
                    id = o.getString("id"),
                    title = o.getString("title"),
                    message = o.getString("message"),
                    timeMillis = o.getLong("time"),
                    read = o.getBoolean("read")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(list: List<AppNotification>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("message", it.message)
                    .put("time", it.timeMillis)
                    .put("read", it.read)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object {
        const val KEY = "items"
    }
}