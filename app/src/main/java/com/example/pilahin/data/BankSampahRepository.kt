package com.example.pilahin.data

import kotlinx.coroutines.delay

data class LatLng(val latitude: Double, val longitude: Double)

data class BankSampah(
    val id: String,
    val name: String,
    val location: LatLng
)

/** Kontrak data bank sampah. Nanti diganti implementasi backend / API asli. */
interface BankSampahRepository {
    suspend fun getNearby(center: LatLng): List<BankSampah>
}

/** Data contoh di sekitar posisi pengguna, hanya untuk tampilan. */
class FakeBankSampahRepository : BankSampahRepository {
    override suspend fun getNearby(center: LatLng): List<BankSampah> {
        delay(400)
        return listOf(
            BankSampah("1", "Bank Sampah Melati", LatLng(center.latitude + 0.0012, center.longitude - 0.0010)),
            BankSampah("2", "Bank Sampah Hijau", LatLng(center.latitude + 0.0005, center.longitude + 0.0014)),
            BankSampah("3", "Bank Sampah Lestari", LatLng(center.latitude - 0.0013, center.longitude - 0.0006))
        )
    }
}