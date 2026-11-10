package com.example.pilahin.data

import java.io.File
import java.util.UUID
import kotlinx.coroutines.delay

/** Kontrak klasifikasi. Nanti diganti implementasi Retrofit ke Flask + YOLOv8. */
interface ClassifierRepository {
    suspend fun classify(image: File): ScanResult
}

/** Simulasi: hasil dipilih acak, hanya untuk menguji alur aplikasi. */
class FakeClassifierRepository : ClassifierRepository {
    private val samples = listOf(
        "Botol plastik" to WasteCategory.ANORGANIK,
        "Kaleng minuman" to WasteCategory.ANORGANIK,
        "Kulit pisang" to WasteCategory.ORGANIK,
        "Sisa nasi" to WasteCategory.ORGANIK,
        "Baterai bekas" to WasteCategory.B3,
        "Tisu kotor" to WasteCategory.RESIDU
    )

    override suspend fun classify(image: File): ScanResult {
        delay(1500)
        val (name, category) = samples.random()
        return ScanResult(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            confidence = (85..98).random() / 100f,
            imagePath = image.absolutePath,
            timeMillis = System.currentTimeMillis()
        )
    }
}