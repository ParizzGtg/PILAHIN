package com.example.pilahin.ui.pickup

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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.data.PickupStatus
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal val idLocale = Locale("id", "ID")

internal fun formatDay(millis: Long): String =
    SimpleDateFormat("EEEE, d MMM yyyy", idLocale).format(Date(millis))

internal fun formatDateTime(millis: Long): String =
    SimpleDateFormat("d MMM, HH.mm", idLocale).format(Date(millis))

private fun dayAbbrev(millis: Long): String =
    SimpleDateFormat("EEE", idLocale).format(Date(millis)).uppercase(idLocale)

private fun dayNumber(millis: Long): String =
    SimpleDateFormat("d", idLocale).format(Date(millis))

private fun slotLabel(slot: String): String =
    if (slot.contains("–")) slot else "Pukul $slot"

private fun isActive(r: PickupRequest) =
    r.status == PickupStatus.MENUNGGU || r.status == PickupStatus.DITERIMA

private fun statusColors(status: PickupStatus): Pair<Color, Color> = when (status) {
    PickupStatus.MENUNGGU -> Color(0xFFE66A00) to Color(0xFFFFEBD6)
    PickupStatus.DITERIMA -> Color(0xFF2E7D32) to Color(0xFFE4F1D4)
    PickupStatus.DITOLAK -> Color(0xFFC62828) to Color(0xFFFFEDEA)
    PickupStatus.SELESAI -> Color(0xFF1E7FD1) to Color(0xFFE1EFFB)
    PickupStatus.DIBATALKAN -> Color(0xFF616161) to Color(0xFFEEEEEE)
}

@Composable
fun PickupScreen(
    viewModel: PickupViewModel,
    onNewRequest: () -> Unit
) {
    val requests by viewModel.requests.collectAsState()

    // Yang aktif di atas (urut tanggal terdekat), sisanya menyusul (terbaru dulu)
    val schedule = requests.sortedWith(
        compareBy<PickupRequest> { !isActive(it) }
            .thenBy { if (isActive(it)) it.dayMillis else -it.createdMillis }
    )

    var cancelTarget by remember { mutableStateOf<PickupRequest?>(null) }
    var reminderOn by rememberSaveable { mutableStateOf(true) }
    var reminderIndex by rememberSaveable { mutableStateOf(0) }
    val reminderOptions = listOf("1 hari sebelumnya", "1 jam sebelumnya", "30 menit sebelumnya")

    ScreenColumn(title = "Jemput Sampah", onBack = null) {
        Reveal(0) {
            Button(
                onClick = onNewRequest,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Rounded.LocalShipping, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Request Jemput", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = "Riwayat jadwal penjemputan",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.reveal(1)
        )
        Text(
            text = "Semua request yang kamu buat tercatat di sini.",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.reveal(1)
        )
        Spacer(Modifier.height(10.dp))

        if (schedule.isEmpty()) {
            InfoCard(modifier = Modifier.reveal(2)) {
                Text("Belum ada jadwal penjemputan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(
                    text = "Tekan \"Request Jemput\", isi alamat dan tanggal. Jadwalnya akan muncul di sini.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(10.dp))
        } else {
            schedule.forEachIndexed { i, request ->
                ScheduleCard(
                    request = request,
                    modifier = Modifier.reveal(minOf(i + 2, 9)),
                    onCancel = if (isActive(request)) ({ cancelTarget = request }) else null,
                    onDelete = if (!isActive(request)) ({ viewModel.delete(request.id) }) else null
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        // Pengingat
        Spacer(Modifier.height(6.dp))
        InfoCard(modifier = Modifier.reveal(minOf(schedule.size + 2, 9))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pengingat jadwal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("Ingatkan sebelum petugas datang.", fontSize = 12.sp, color = TextMuted)
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
                reminderOptions.forEachIndexed { i, label ->
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

        Spacer(Modifier.height(20.dp))
    }

    cancelTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            containerColor = Color.White,
            title = { Text("Batalkan request?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Request jemput ${formatDay(target.dayMillis)} akan dibatalkan.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancel(target.id)
                    cancelTarget = null
                }) { Text("Batalkan request", color = Color(0xFFC62828), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text("Kembali", color = TextMuted) }
            }
        )
    }
}

@Composable
private fun ScheduleCard(
    request: PickupRequest,
    modifier: Modifier,
    onCancel: (() -> Unit)?,
    onDelete: (() -> Unit)?
) {
    val (fg, bg) = statusColors(request.status)
    val active = isActive(request)

    InfoCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (active) Lime else Color(0xFFEAF6DD)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(dayAbbrev(request.dayMillis), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                Text(dayNumber(request.dayMillis), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDay(request.dayMillis),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                // Jam ditetapkan admin saat menyetujui
                if (request.slot.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, null, tint = GreenPrimary, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Dijemput ${slotLabel(request.slot).lowercase(idLocale)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenDark
                        )
                    }
                } else if (request.status == PickupStatus.MENUNGGU) {
                    Text("Jam ditentukan admin setelah disetujui", fontSize = 11.sp, color = TextMuted)
                }
            }
            if (onDelete != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Delete, "Hapus", tint = TextMuted, modifier = Modifier.size(22.dp))
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.LocationOn, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    text = request.address,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (request.detail.isNotBlank()) {
                    Text(request.detail, fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = request.status.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            request.types.forEach { CategoryChip(it) }
        }

        if (request.adminNote.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Catatan admin: ${request.adminNote}",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )
        }

        if (onCancel != null) {
            Spacer(Modifier.height(2.dp))
            TextButton(onClick = onCancel) {
                Text("Batalkan request", color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}