package com.example.pilahin.data

enum class WasteCategory(
    val label: String,
    val description: String,
    val example: String,
    val tip: String,
    val steps: List<String>
) {
    ORGANIK(
        "Organik",
        "Berasal dari makhluk hidup dan mudah terurai.",
        "Sisa makanan, kulit buah, daun kering.",
        "Pisahkan dari sampah kering, lalu olah jadi kompos jika memungkinkan.",
        listOf(
            "Buang bagian yang tidak bisa dimakan",
            "Tiriskan cairan berlebih",
            "Masukkan ke wadah sampah organik",
            "Olah menjadi kompos bila memungkinkan"
        )
    ),
    ANORGANIK(
        "Anorganik",
        "Sulit terurai, banyak yang bisa didaur ulang.",
        "Botol plastik, kaleng, kertas, kaca.",
        "Bilas dan keringkan agar mudah didaur ulang.",
        listOf(
            "Kosongkan isi botol",
            "Bilas seperlunya",
            "Pipihkan agar hemat ruang",
            "Setorkan ke bank sampah terdekat"
        )
    ),
    B3(
        "B3",
        "Bahan berbahaya dan beracun, perlu penanganan khusus.",
        "Baterai, lampu neon, obat kedaluwarsa, pestisida.",
        "Jangan dicampur dengan sampah lain dan jangan dibakar.",
        listOf(
            "Jangan dibuang bersama sampah biasa",
            "Kumpulkan dalam wadah tertutup",
            "Simpan jauh dari jangkauan anak",
            "Serahkan ke titik pengumpulan B3"
        )
    ),
    RESIDU(
        "Residu",
        "Tidak bisa didaur ulang maupun dikomposkan.",
        "Tisu kotor, popok, puntung rokok, pembalut.",
        "Kurangi pemakaiannya dan bungkus rapat sebelum dibuang.",
        listOf(
            "Pastikan benar-benar tidak bisa dipilah",
            "Bungkus rapat agar tidak berceceran",
            "Masukkan ke tempat sampah residu",
            "Kurangi pemakaian barang sekali pakai"
        )
    )
}

data class ScanResult(
    val id: String,
    val name: String,
    val category: WasteCategory,
    val confidence: Float,
    val imagePath: String,
    val timeMillis: Long
)