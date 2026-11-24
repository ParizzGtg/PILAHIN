package com.example.pilahin.data

import android.content.Context
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class WastePrice(
    val id: String,      // id dari server (dipakai untuk PUT)
    val code: String,    // kode, mis. P01B
    val group: String,   // Plastik, Logam, Kertas, Botol Kaca, Minyak Jelantah
    val name: String,
    val example: String,
    val unit: String,    // "kg" atau "pcs"
    val price: Int
) {
    val unitLabel: String get() = if (unit.equals("pcs", true)) "pcs" else "kg"
}

fun rupiah(n: Int): String =
    "Rp " + NumberFormat.getInstance(Locale("id", "ID")).format(n)

/** Daftar harga UPT Bank Sampah T.A 2025 (berlaku mulai Oktober 2025). Cadangan bila server belum siap. */
private fun d(code: String, group: String, name: String, example: String, unit: String, price: Int) =
    WastePrice(code, code, group, name, example, unit, price)

val DefaultPrices: List<WastePrice> = listOf(
    d("P01B", "Plastik", "PP Gelas Bening Bersih", "Aqua, Club, JS tanpa label", "kg", 4000),
    d("P01K", "Plastik", "PP Gelas Bening Kotor", "Aqua, Club, JS dengan label", "kg", 1500),
    d("P02B", "Plastik", "PP Gelas Warna", "Ale-Ale, Montea, teh gelas", "kg", 1300),
    d("P02P", "Plastik", "PP Pipet", "Sedotan atau pipet warna selain hitam", "kg", 1000),
    d("P02C", "Plastik", "PP Cincin Gelas", "Cincin atau potongan bibir gelas", "kg", 1200),
    d("P03B", "Plastik", "PET Bening Bersih", "Botol ades, fanta, pulpy tanpa penutup dan label", "kg", 5600),
    d("P04B", "Plastik", "PET Biru Muda Bersih", "Botol air mineral Aqua, JS, Club tanpa penutup dan label", "kg", 4800),
    d("P04K", "Plastik", "PET Kotor", "Segala jenis PET yang masih ada label dan penutup", "kg", 1000),
    d("P05B", "Plastik", "PET Warna Bersih/Pisah", "PET tanpa penutup dan label, warna hijau atau biru", "kg", 1500),
    d("P05C", "Plastik", "PET Warna Campur", "PET tanpa penutup dan label, hijau dan biru (campur)", "kg", 1300),
    d("P04C", "Plastik", "PET Campur", "PET campur tanpa penutup dan label", "kg", 1200),
    d("P05", "Plastik", "PET Toples", "Toples dari bahan PET, tanpa tutup", "kg", 1500),
    d("P06", "Plastik", "Plastik HD Campur", "Baskom, gelas, piring, tanpa damar", "kg", 1400),
    d("P06C", "Plastik", "HD Tutup Botol", "Tutup botol campur", "kg", 2000),
    d("P09K", "Plastik", "HD Tutup Galon", "Tutup galon warna campur", "kg", 2500),
    d("P13", "Plastik", "Plastik HD (Blow) Campur", "Kemasan plastik bergaris tengah", "kg", 1900),

    d("L01", "Logam", "Besi Tebal", "Besi cor, besi plat", "kg", 3200),
    d("L02", "Logam", "Besi Tipis", "Drum, rak piring", "kg", 2000),
    d("L03", "Logam", "Kaleng", "Kaleng makanan, kaleng susu, tidak dipress", "kg", 1000),
    d("L04", "Logam", "Kuningan", "Kuningan piala", "kg", 50000),
    d("L05", "Logam", "Tembaga", "Tembaga dari kabel", "kg", 85000),
    d("L06", "Logam", "Aluminium Tebal", "Box mesin motor", "kg", 18000),
    d("L07", "Logam", "Aluminium Tipis", "Kaleng minuman, Fanta, Sprite, wajan", "kg", 12000),
    d("L08", "Logam", "Aluminium Siku", "Alma dekor", "kg", 20000),
    d("L09", "Logam", "Aluminium Campur", "Aluminium campur besi", "kg", 9000),
    d("L11", "Logam", "Besi Seng", "Seng bekas", "kg", 1000),
    d("L10", "Logam", "Perunggu", "Kran air, kepala regulator", "kg", 8500),

    d("K01", "Kertas", "Kertas Putih", "Buku tulis, kertas foto copy", "kg", 1400),
    d("K02", "Kertas", "Kertas Campur/Warna", "Majalah, karton warna, mikel", "kg", 500),
    d("K03", "Kertas", "Kertas Buram", "Kertas kelabu / buram", "kg", 1100),
    d("K04", "Kertas", "Kardus (Dos)", "Karton coklat box", "kg", 1400),
    d("K05", "Kertas", "Kertas Semen", "Kertas kemasan semen", "kg", 400),
    d("K06", "Kertas", "Koran", "Koran berita", "kg", 1200),
    d("K08", "Kertas", "Cones", "Kertas gulungan bulat", "kg", 300),

    d("B01", "Botol Kaca", "Botol Markisa, Bensin", "Botol leher pendek bening", "pcs", 900),
    d("B02", "Botol Kaca", "Botol Kecap / Bir", "Botol leher panjang tebal, Angke, Bintang", "pcs", 600),
    d("B03", "Botol Kaca", "Botol Anggur", "Anggur merah, Singaraja, Kolesom, Provost, Wijen", "pcs", 250),
    d("B05", "Botol Kaca", "Botol Bir Guinness Kecil", "Botol Guinness ukuran kecil", "pcs", 300),
    d("B06", "Botol Kaca", "Botol Bir Guinness", "Botol Guinness ukuran besar", "pcs", 400),

    d("M01", "Minyak Jelantah", "Minyak Jelantah", "Minyak goreng bekas memasak/goreng", "kg", 4000)
)

private fun groupFromCode(code: String): String = when (code.uppercase().firstOrNull()) {
    'P' -> "Plastik"
    'L' -> "Logam"
    'K' -> "Kertas"
    'B' -> "Botol Kaca"
    'M' -> "Minyak Jelantah"
    else -> "Lainnya"
}

private fun JSONObject.firstString(vararg keys: String): String {
    for (k in keys) {
        if (has(k) && !isNull(k)) {
            val v = opt(k)?.toString().orEmpty()
            if (v.isNotBlank()) return v
        }
    }
    return ""
}

/** SATU-SATUNYA tempat yang perlu dicocokkan dengan bentuk JSON dari backend. */
private fun parseOne(o: JSONObject): WastePrice? {
    val code = o.firstString("kode", "code")
    val id = o.firstString("id").ifBlank { code }
    val name = o.firstString("nama", "name", "barang")
    if (name.isBlank()) return null
    val price = (o.optDouble("harga", o.optDouble("price", Double.NaN))).let {
        if (it.isNaN()) return null else it.toInt()
    }
    return WastePrice(
        id = id,
        code = code,
        group = o.firstString("kelompok", "group", "kategori", "category").ifBlank { groupFromCode(code) },
        name = name,
        example = o.firstString("contoh", "example"),
        unit = o.firstString("satuan", "unit").lowercase().ifBlank { "kg" },
        price = price
    )
}

private fun parseList(arr: JSONArray?): List<WastePrice> {
    if (arr == null) return emptyList()
    return (0 until arr.length()).mapNotNull { i ->
        runCatching { parseOne(arr.getJSONObject(i)) }.getOrNull()
    }
}

private fun findArray(body: JSONObject): JSONArray? =
    listOf("prices", "data", "harga", "harga_sampah", "items")
        .firstNotNullOfOrNull { body.optJSONArray(it) }

/** Sumber data harga: server (admin bisa ubah), dengan cache dan daftar bawaan sebagai cadangan. */
object PriceStore {

    private val _prices = MutableStateFlow(DefaultPrices)
    val prices: StateFlow<List<WastePrice>> = _prices.asStateFlow()

    /** true kalau data berasal dari server (atau cache server). */
    private val _fromServer = MutableStateFlow(false)
    val fromServer: StateFlow<Boolean> = _fromServer.asStateFlow()

    private var cacheLoaded = false

    private fun prefs(c: Context) =
        c.applicationContext.getSharedPreferences("pilahin_prices", Context.MODE_PRIVATE)

    private fun loadCache(c: Context) {
        if (cacheLoaded) return
        cacheLoaded = true
        val raw = prefs(c).getString("list", null) ?: return
        val list = runCatching { parseList(JSONArray(raw)) }.getOrDefault(emptyList())
        if (list.isNotEmpty()) _prices.value = list
    }

    private fun saveCache(c: Context, list: List<WastePrice>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject().put("id", it.id).put("kode", it.code).put("kelompok", it.group)
                    .put("nama", it.name).put("contoh", it.example).put("satuan", it.unit)
                    .put("harga", it.price)
            )
        }
        prefs(c).edit().putString("list", arr.toString()).apply()
    }

    suspend fun refresh(context: Context) {
        loadCache(context)
        val token = SessionManager(context).token ?: return
        val res = ApiClient.request("GET", "/api/harga-sampah", token = token)
        if (!res.isSuccess) return
        val list = parseList(findArray(res.body))
        if (list.isEmpty()) return
        _prices.value = list
        _fromServer.value = true
        saveCache(context, list)
    }

    /** Admin: ubah harga. Mengembalikan null kalau berhasil, atau pesan error. */
    suspend fun update(context: Context, id: String, newPrice: Int): String? {
        val token = SessionManager(context).token ?: return "Sesi tidak valid, silakan login ulang"
        val body = JSONObject().put("harga", newPrice).put("price", newPrice)
        val res = ApiClient.request("PUT", "/api/admin/harga-sampah/$id", body, token)
        if (!res.isSuccess) return res.message
        val list = _prices.value.map { if (it.id == id) it.copy(price = newPrice) else it }
        _prices.value = list
        saveCache(context, list)
        return null
    }

    /** Pilih baris harga yang paling relevan untuk nama benda hasil scan. */
    fun match(name: String, list: List<WastePrice>): List<WastePrice> {
        val n = name.lowercase()
        val codes: List<String> = when {
            "jelantah" in n || "minyak" in n -> listOf("M01")
            "kaca" in n || "beling" in n || "guinness" in n -> listOf("B01", "B02", "B03", "B05", "B06")
            "tutup" in n && "galon" in n -> listOf("P09K")
            "tutup" in n -> listOf("P06C", "P09K")
            "galon" in n -> listOf("P09K")
            "gelas" in n -> listOf("P01B", "P01K", "P02B")
            "pipet" in n || "sedotan" in n -> listOf("P02P")
            "botol" in n -> listOf("P03B", "P04B", "P05B", "P04C", "P04K")
            "plastik" in n -> listOf("P03B", "P04B", "P06", "P13")
            "kaleng" in n && ("minuman" in n || "soda" in n) -> listOf("L07")
            "kaleng" in n -> listOf("L07", "L03")
            "aluminium" in n || "alumunium" in n -> listOf("L06", "L07", "L08", "L09")
            "tembaga" in n -> listOf("L05")
            "kuningan" in n -> listOf("L04")
            "perunggu" in n -> listOf("L10")
            "seng" in n -> listOf("L11")
            "besi" in n || "logam" in n -> listOf("L01", "L02", "L11")
            "kardus" in n || "karton" in n || "dos" in n -> listOf("K04", "K05")
            "koran" in n -> listOf("K06")
            "majalah" in n -> listOf("K02")
            "kertas" in n || "buku" in n -> listOf("K01", "K02", "K03", "K06")
            else -> emptyList()
        }
        return codes.mapNotNull { c -> list.firstOrNull { it.code.equals(c, ignoreCase = true) } }
    }
}