package com.example.pilahin.data

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import org.json.JSONObject

data class LatLng(val latitude: Double, val longitude: Double)

data class BankSampah(
    val id: String,
    val name: String,
    val location: LatLng,
    val address: String? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val distanceKm: Double? = null,
    val source: String = "openstreetmap"
)

/** Jarak garis lurus antara dua titik (km). */
fun LatLng.distanceKmTo(other: LatLng): Double {
    val r = 6371.0088
    val p1 = Math.toRadians(latitude)
    val p2 = Math.toRadians(other.latitude)
    val dp = p2 - p1
    val dl = Math.toRadians(other.longitude - longitude)
    val a = sin(dp / 2) * sin(dp / 2) + cos(p1) * cos(p2) * sin(dl / 2) * sin(dl / 2)
    return 2 * r * asin(sqrt(a))
}

/** Kontrak data bank sampah. */
interface BankSampahRepository {
    suspend fun getNearby(center: LatLng): List<BankSampah>
}

/**
 * Data ASLI dari server PILAHIN (GET /api/bank-sampah).
 * Server mencari bank sampah di OpenStreetMap sekitar lokasi GPS pengguna,
 * lalu mengurutkannya dari yang paling dekat.
 */
class ApiBankSampahRepository(
    private val radiusKm: Int = RADIUS_KM
) : BankSampahRepository {

    override suspend fun getNearby(center: LatLng): List<BankSampah> {
        val path = "/api/bank-sampah?lat=${center.latitude}&lng=${center.longitude}&radius_km=$radiusKm"
        val res = ApiClient.request("GET", path)
        if (!res.isSuccess) throw ApiException(res.message, res.code)

        val arr = res.body.optJSONArray("banks") ?: return emptyList()
        return (0 until arr.length()).map { i -> parse(arr.getJSONObject(i)) }
    }

    private fun parse(o: JSONObject): BankSampah = BankSampah(
        id = o.getString("id"),
        name = o.getString("name"),
        location = LatLng(o.getDouble("latitude"), o.getDouble("longitude")),
        address = o.optText("address"),
        phone = o.optText("phone"),
        openingHours = o.optText("opening_hours"),
        distanceKm = if (o.isNull("distance_km")) null else o.getDouble("distance_km"),
        source = o.optString("source", "openstreetmap")
    )

    /** optString mengembalikan teks "null" untuk nilai null, jadi dicek dulu. */
    private fun JSONObject.optText(key: String): String? =
        if (isNull(key)) null else optString(key).ifBlank { null }

    companion object {
        const val RADIUS_KM = 25
    }
}