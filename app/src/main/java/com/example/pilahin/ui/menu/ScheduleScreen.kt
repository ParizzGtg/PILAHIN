package com.example.pilahin.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

private data class Pickup(
    val day: String,
    val date: String,
    val fullDate: String,
    val time: String,
    val type: WasteCategory
)

// Data contoh, nanti dari backend
private val pickups = listOf(
    Pickup("SAB", "31", "Sabtu, 31 Oktober 2026", "07.00–09.00", WasteCategory.ORGANIK),
    Pickup("SEL", "03", "Selasa, 3 November 2026", "07.00–09.00", WasteCategory.ANORGANIK),
    Pickup("SAB", "07", "Sabtu, 7 November 2026", "01.00–08.00", WasteCategory.ORGANIK)
)

@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    var reminderOn by rememberSaveable { mutableStateOf(true) }
    var reminderIndex by rememberSaveable { mutableStateOf(0) }
    val options = listOf("1 hari sebelumnya", "1 jam sebelumnya", "30 menit sebelumnya")

    ScreenColumn(title = "Jadwal Pengangkutan", onBack = onBack) {
        Row(
            modifier = Modifier
                .reveal(0)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.LocationOn, null, tint = GreenPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Melati RT 03", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
        }
        Spacer(Modifier.height(12.dp))

        pickups.forEachIndexed { index, p ->
            InfoCard(modifier = Modifier.reveal(index + 1)) {
                if (index == 0) {
                    Text(
                        text = "PENGANGKUTAN TERDEKAT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GreenDark
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (index == 0) Lime else Color(0xFFEAF6DD)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(p.day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                        Text(p.date, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.fullDate, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(p.time, fontSize = 12.sp, color = TextMuted)
                    }
                    CategoryChip(p.type)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        InfoCard(modifier = Modifier.reveal(pickups.size + 1)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pengingat pengangkutan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(
                        text = "Ingatkan saya sebelum petugas mengambil sampah.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = reminderOn,
                    onCheckedChange = { reminderOn = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = GreenPrimary,
                        checkedThumbColor = Color.White
                    )
                )
            }
            if (reminderOn) {
                Spacer(Modifier.height(6.dp))
                options.forEachIndexed { i, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { reminderIndex = i },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reminderIndex == i,
                            onClick = { reminderIndex = i },
                            colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                        )
                        Text(label, fontSize = 13.sp, color = TextDark)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Reveal(pickups.size + 2) {
            Text(
                text = "Jadwal dapat berubah sewaktu-waktu mengikuti pengumuman pengelola setempat.",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}