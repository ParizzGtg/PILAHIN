package com.example.pilahin.ui.menu

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.ui.common.CardBorderStroke
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun ProfileScreen(
    name: String,
    username: String,
    onHistory: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    fun soon(label: String) =
        Toast.makeText(context, "$label segera hadir", Toast.LENGTH_SHORT).show()

    ScreenColumn(title = "Profil", onBack = null) {
        Reveal(0) {
            Text("Kelola akun dan preferensimu.", fontSize = 13.sp, color = TextMuted)
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(1)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFE4F1D4))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(username, fontSize = 12.sp, color = TextMuted)
            }
            Text(
                text = "Ubah Profil",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenDark,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Lime)
                    .clickable { soon("Ubah profil") }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        }

        Spacer(Modifier.height(18.dp))
        Reveal(2) {
            Column {
                SectionTitle("AKTIVITAS & JADWAL")
                MenuGroup {
                    MenuRow(Icons.Rounded.LocationOn, "Alamat Pengangkutan", "Melati RT 03") { soon("Alamat pengangkutan") }
                    MenuRow(Icons.Rounded.Notifications, "Pengingat & Notifikasi", "Penyesuaian jadwal pengingat") { soon("Pengingat") }
                    MenuRow(Icons.Rounded.History, "Riwayat Pemilahan", null, onClick = onHistory)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Reveal(3) {
            Column {
                SectionTitle("AKUN & DUKUNGAN")
                MenuGroup {
                    MenuRow(Icons.Rounded.Lock, "Akun & Privasi", null) { soon("Akun & privasi") }
                    MenuRow(Icons.Rounded.Help, "Bantuan", null) { soon("Bantuan") }
                    MenuRow(Icons.Rounded.Info, "Info Aplikasi", "Deskripsi dan pencipta", onClick = onAbout)
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(4)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFFFEDEA))
                .clickable { showLogoutDialog = true }
                .padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Keluar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
        }

        Spacer(Modifier.height(14.dp))
        Reveal(5) {
            Text(
                text = "Pilah sampah, hijaukan bumi",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(20.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            title = { Text("Keluar dari akun?", fontWeight = FontWeight.Bold) },
            text = { Text("Anda perlu login lagi untuk memakai PILAHIN.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Keluar", color = Color(0xFFC62828), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun MenuGroup(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = CardBorderStroke,
        shadowElevation = 2.dp
    ) {
        Column { content() }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(ChipBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            if (subtitle != null) Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted)
    }
}