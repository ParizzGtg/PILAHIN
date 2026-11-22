package com.example.pilahin.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class PickupStatus(val label: String) {
    MENUNGGU("Menunggu konfirmasi"),
    DITERIMA("Disetujui"),
    DITOLAK("Ditolak"),
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
    /** Jam penjemputan yang ditetapkan admin, misalnya "09.30". Kosong = belum ditetapkan. */
    val slot: String,
    val types: List<WasteCategory>,
    val note: String,
    val status: PickupStatus,
    val createdMillis: Long,
    val adminNote: String = "",
    val requesterName: String = "",
    val requesterPhone: String = ""
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeMillis: Long,
    val read: Boolean
)

/* ---------- JSON <-> PickupRequest (dipakai server dan cache lokal) ---------- */

fun pickupFromJson(o: JSONObject): PickupRequest {
    val arr = o.optJSONArray("types")
    val raw = if (arr != null) {
        (0 until arr.length()).map { arr.optString(it) }
    } else {
        o.optString("types").split(",")
    }
    val types = raw.mapNotNull { s ->
        runCatching { WasteCategory.valueOf(s.trim().uppercase()) }.getOrNull()
    }
    return PickupRequest(
        id = o.opt("id")?.toString() ?: "",
        address = o.optString("address"),
        detail = o.optString("detail"),
        latitude = o.optDouble("latitude", o.optDouble("lat", 0.0)),
        longitude = o.optDouble("longitude", o.optDouble("lon", 0.0)),
        dayMillis = o.optLong("day"),
        slot = o.optString("pickup_time").ifBlank { o.optString("slot") },
        types = types,
        note = o.optString("note"),
        status = runCatching { PickupStatus.valueOf(o.optString("status").uppercase()) }
            .getOrDefault(PickupStatus.MENUNGGU),
        createdMillis = o.optLong("created"),
        adminNote = o.optString("admin_note"),
        requesterName = o.optString("requester_name"),
        requesterPhone = o.optString("requester_phone")
    )
}

fun pickupToJson(r: PickupRequest): JSONObject = JSONObject()
    .put("id", r.id)
    .put("address", r.address)
    .put("detail", r.detail)
    .put("latitude", r.latitude)
    .put("longitude", r.longitude)
    .put("day", r.dayMillis)
    .put("pickup_time", r.slot)
    .put("types", JSONArray(r.types.map { it.name }))
    .put("note", r.note)
    .put("status", r.status.name)
    .put("created", r.createdMillis)
    .put("admin_note", r.adminNote)
    .put("requester_name", r.requesterName)
    .put("requester_phone", r.requesterPhone)

fun parsePickups(arr: JSONArray?): List<PickupRequest> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).mapNotNull { i ->
        runCatching { pickupFromJson(arr.getJSONObject(i)) }.getOrNull()
    }
}

/** Cache lokal. Sumber data utama sekarang dari server. */
class PickupRepository(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("pilahin_pickup", Context.MODE_PRIVATE)

    fun getAll(): List<PickupRequest> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            parsePickups(JSONArray(raw))
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveAll(list: List<PickupRequest>) {
        val arr = JSONArray()
        list.forEach { arr.put(pickupToJson(it)) }
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