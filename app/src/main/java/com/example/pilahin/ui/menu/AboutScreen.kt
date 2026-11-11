package com.example.pilahin.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.R
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

private data class Creator(val name: String, val role: String)

private val creators = listOf(
    Creator("Paris Septio", "Pengembang Aplikasi Android (Frontend)"),
    Creator("Ferdi Seswanto", "Pengembang Aplikasi Android (Backend & AI)"),
    Creator("Salsa Bila", "Testing Aplikasi")
)

private const val CAMPUS = "Politeknik Negeri Ujung Pandang"
private const val COURSE = "Mobile Application — Project-based Learning"

private val features = listOf(
    "Scan Sampah dengan kamera atau galeri menggunakan AI (YOLOv8)",
    "Panduan Pemilahan: organik, anorganik, B3, dan residu",
    "Riwayat Klasifikasi yang bisa disaring per kategori",
    "Jadwal Pengangkutan beserta pengingat",
    "Peta Bank Sampah terdekat berdasarkan lokasi GPS",
    "Edukasi pengelolaan minyak jelantah"
)

private val technologies = listOf(
    "Jetpack Compose + Material 3",
    "MVVM + Repository Pattern",
    "Kotlin Coroutines + Flow",
    "CameraX dan osmdroid (peta)",
    "YOLOv8 (deteksi jenis sampah)"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    ScreenColumn(title = "Info Aplikasi", onBack = onBack) {
        Reveal(0) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_pilahin),
                    contentDescription = "Logo Pilahin",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(150.dp)
                )
                Text(
                    text = "Versi $version",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        InfoCard(modifier = Modifier.reveal(1)) {
            Text("Deskripsi Aplikasi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "PILAHIN adalah aplikasi edukasi pemilahan sampah. Pengguna cukup memotret " +
                        "sampah, lalu kecerdasan buatan mengenali jenisnya dan menjelaskan cara " +
                        "memilah serta membuangnya dengan tepat. Aplikasi ini juga membantu " +
                        "menemukan bank sampah terdekat dan mengingatkan jadwal pengangkutan, " +
                        "agar memilah sampah menjadi kebiasaan yang mudah bagi masyarakat.",
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        InfoCard(modifier = Modifier.reveal(2)) {
            Text("Fitur Utama", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(8.dp))
            features.forEach { BulletRow(it) }
        }

        Spacer(Modifier.height(10.dp))

        InfoCard(modifier = Modifier.reveal(3)) {
            Text("Pencipta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(10.dp))
            creators.forEach { creator ->
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = creator.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(creator.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(creator.role, fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(CAMPUS, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GreenDark)
            Text(COURSE, fontSize = 12.sp, color = TextMuted)
        }

        Spacer(Modifier.height(10.dp))

        InfoCard(modifier = Modifier.reveal(4)) {
            Text("Teknologi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(8.dp))
            technologies.forEach { BulletRow(it) }
        }

        Spacer(Modifier.height(14.dp))
        Reveal(5) {
            Text(
                text = "Pilah sampah, hijaukan bumi.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun BulletRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Lime)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 13.sp, color = TextDark, lineHeight = 19.sp)
    }
}