package com.example.pilahin.ui.common

import android.content.Context
import android.location.Geocoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ubah koordinat GPS jadi teks alamat. Kalau gagal, hasilnya koordinat. */
@Suppress("DEPRECATION")
suspend fun fetchAddressText(context: Context, lat: Double, lon: Double): String =
    withContext(Dispatchers.IO) {
        val fallback = "%.5f, %.5f".format(Locale.US, lat, lon)
        try {
            if (!Geocoder.isPresent()) return@withContext fallback
            Geocoder(context, Locale("id", "ID"))
                .getFromLocation(lat, lon, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
                ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }

private const val PREFS = "pilahin_app"
private const val KEY_ADDRESS = "last_address"

/** Alamat GPS terakhir, supaya langsung tampil saat layar dibuka. */
fun loadSavedAddress(context: Context): String? =
    context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_ADDRESS, null)

fun saveAddress(context: Context, address: String) {
    context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit().putString(KEY_ADDRESS, address).apply()
}