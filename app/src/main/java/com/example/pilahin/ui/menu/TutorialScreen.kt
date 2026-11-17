package com.example.pilahin.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

private data class TutorialStep(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val steps = listOf(
    TutorialStep(
        Icons.Rounded.Login,
        "Daftar dan masuk",
        "Buat akun dengan email atau akun Google, lalu login. Setelah itu kamu langsung masuk ke Beranda."
    ),
    TutorialStep(
        Icons.Rounded.DocumentScanner,
        "Scan sampah",
        "Ketuk tombol Scan di tengah bawah. Arahkan kamera ke satu objek sampah lalu tekan tombol bulat. Bisa juga pilih foto dari Galeri."
    ),
    TutorialStep(
        Icons.Rounded.MenuBook,
        "Baca edukasi pemilahan",
        "Setelah scan, kartu berwarna menunjukkan jenis sampah, wadah yang tepat, langkah memilah, dan hal yang perlu dihindari."
    ),
    TutorialStep(
        Icons.Rounded.History,
        "Simpan ke riwayat",
        "Tekan Simpan ke Riwayat agar hasil scan tercatat. Buka tab Riwayat untuk melihat ulang dan menyaring per kategori."
    ),
    TutorialStep(
        Icons.Rounded.Place,
        "Cari bank sampah terdekat",
        "Izinkan lokasi, lalu peta di Beranda menampilkan bank sampah di sekitarmu. Ketuk peta untuk membukanya layar penuh."
    ),
    TutorialStep(
        Icons.Rounded.LocalShipping,
        "Request jemput sampah",
        "Buka tab Jemput lalu Request Jemput. Alamat terisi otomatis dari GPS. Pilih tanggal, isi jam, pilih jenis sampah, lalu kirim."
    ),
    TutorialStep(
        Icons.Rounded.Notifications,
        "Pantau notifikasi",
        "Ketuk ikon lonceng di Beranda untuk melihat kabar status request jemputmu. Titik hijau berarti ada yang belum dibaca."
    ),
    TutorialStep(
        Icons.Rounded.Person,
        "Atur profil",
        "Di tab Profil, ketuk Ubah Profil untuk mengganti foto, nama, username, email, dan password."
    )
)

@Composable
fun TutorialScreen(onBack: () -> Unit) {
    ScreenColumn(title = "Tutorial Penggunaan", onBack = onBack) {
        Reveal(0) {
            Column {
                Text(
                    text = "Mulai dari sini",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Ikuti ${steps.size} langkah singkat untuk memakai PILAHIN.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        steps.forEachIndexed { index, step ->
            InfoCard(modifier = Modifier.reveal(minOf(index + 1, 9))) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ChipBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(step.icon, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Lime),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenDark
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = step.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = step.description,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = TextMuted
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(4.dp))
        Reveal(10) {
            Text(
                text = "Pilah sampah, hijaukan bumi.",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}