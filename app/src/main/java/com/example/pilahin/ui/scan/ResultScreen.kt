package com.example.pilahin.ui.scan

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.auth.PrimaryButton
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.color
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.io.File

private fun WasteCategory.binName(): String = when (this) {
    WasteCategory.ORGANIK -> "HIJAU"
    WasteCategory.ANORGANIK -> "KUNING"
    WasteCategory.B3 -> "MERAH KHUSUS"
    WasteCategory.RESIDU -> "ABU-ABU"
}

private fun WasteCategory.binColor(): Color = when (this) {
    WasteCategory.ORGANIK -> Color(0xFF43A047)
    WasteCategory.ANORGANIK -> Color(0xFFFBC02D)
    WasteCategory.B3 -> Color(0xFFE53935)
    WasteCategory.RESIDU -> Color(0xFF78909C)
}

/** Alasan umum kategori; dipakai kalau server tidak mengirim alasan. */
private fun WasteCategory.generalReason(): String = when (this) {
    WasteCategory.ORGANIK ->
        "Berasal dari makhluk hidup (tumbuhan, hewan, atau sisa makanan), sehingga bisa diurai bakteri dan jamur menjadi tanah atau kompos."
    WasteCategory.ANORGANIK ->
        "Dibuat dari bahan buatan atau tambang seperti plastik, logam, atau kaca. Bahan ini sulit terurai alami, tetapi banyak yang bisa didaur ulang."
    WasteCategory.B3 ->
        "Mengandung zat kimia berbahaya atau beracun yang bisa mencemari tanah dan air serta membahayakan kesehatan, sehingga butuh penanganan khusus."
    WasteCategory.RESIDU ->
        "Sudah tercampur atau terkontaminasi, sehingga tidak bisa dikomposkan maupun didaur ulang dan hanya bisa berakhir di tempat pembuangan akhir."
}

private fun WasteCategory.avoid(): String = when (this) {
    WasteCategory.ORGANIK -> "Jangan dicampur dengan plastik atau kaca agar kompos tidak tercemar."
    WasteCategory.ANORGANIK -> "Jangan dibuang dalam keadaan kotor berminyak atau masih berisi cairan."
    WasteCategory.B3 -> "Jangan dibakar, dibuang ke selokan, atau dicampur dengan sampah rumah tangga."
    WasteCategory.RESIDU -> "Jangan dibakar karena asapnya mencemari udara."
}

@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onRescan: () -> Unit,
    onGuide: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val item = state.current
    if (item == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val category = item.category
    val accent = category.color()
    val heroShape = RoundedCornerShape(28.dp)

    // Denyut pelan pada ikon tong agar mata langsung tertuju ke edukasi
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    ScreenColumn(title = "Hasil Klasifikasi", onBack = onBack) {

        // ===== Ringkas: thumbnail + nama =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(0),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = File(item.imagePath),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFE4F1D4))
            )
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = item.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(category)
                    Text(
                        text = "Keyakinan ${(item.confidence * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFEAF6DD))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // ===== Peringatan kalau AI kurang yakin =====
        if (item.uncertain) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFF4E0))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Rounded.Info, null, tint = Color(0xFFE66A00), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "AI kurang yakin dengan foto ini. Coba foto ulang dengan cahaya terang, " +
                            "objek di tengah, dan latar polos.",
                    fontSize = 13.sp,
                    color = TextDark,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ===== KARTU EDUKASI UTAMA: warna solid penuh =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(1)
                .shadow(14.dp, heroShape, ambientColor = accent, spotColor = accent)
                .clip(heroShape)
                .background(accent)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.School, null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "EDUKASI PEMILAHAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Wajib dibaca",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Pesan terbesar: buang ke mana
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(category.binColor()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Delete, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Buang ke wadah",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = category.binName(),
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Label kategori: di-highlight supaya jadi hasil utama
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Category, null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "SAMPAH ${category.label.uppercase()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = accent
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = category.description,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            // Kenapa benda ini masuk kategori tersebut (alasan dari AI, kalau kosong pakai alasan umum)
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Kenapa masuk ${category.label}?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = item.reason ?: category.generalReason(),
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Panel putih: langkah, contoh, tips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Cara memilah yang tepat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(Modifier.height(8.dp))
                // Pakai langkah khusus dari server; kalau kosong pakai langkah umum kategori
                val steps = item.disposal.ifEmpty { category.steps }
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step, fontSize = 14.sp, color = TextDark, lineHeight = 19.sp)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text("Contoh serupa", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
                Text(category.example, fontSize = 13.sp, color = TextDark, lineHeight = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Tips", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
                Text(category.tip, fontSize = 13.sp, color = TextDark, lineHeight = 18.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Hindari
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFF4E0))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Rounded.Warning, null, tint = Color(0xFFE66A00), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Hindari", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE66A00))
                    Text(category.avoid(), fontSize = 13.sp, color = TextDark, lineHeight = 18.sp)
                }
            }
        }

        // ===== Perkiraan nilai jual =====
        Spacer(Modifier.height(12.dp))
        PriceCard(
            name = item.name,
            category = category,
            modifier = Modifier.reveal(2)
        )

        Spacer(Modifier.height(10.dp))
        Reveal(3) {
            Text(
                text = "Hasil ini berupa perkiraan AI. Jika ragu, periksa kembali lewat Panduan Pemilahan.",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Reveal(4) {
            PrimaryButton(
                text = if (state.saved) "Tersimpan di Riwayat ✓" else "Simpan ke Riwayat",
                onClick = { viewModel.saveCurrent() }
            )
        }
        Spacer(Modifier.height(10.dp))
        Reveal(5) {
            OutlinedButton(
                onClick = onGuide,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, ChipBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Rounded.MenuBook, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Lihat panduan lengkap", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))
        Reveal(6) {
            OutlinedButton(
                onClick = onRescan,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, ChipBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Scan ulang", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.DocumentScanner, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(20.dp).navigationBarsPadding())
    }
}