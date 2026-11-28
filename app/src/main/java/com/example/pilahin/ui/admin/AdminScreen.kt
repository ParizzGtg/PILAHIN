package com.example.pilahin.ui.admin

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.data.PickupStatus
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Danger = Color(0xFFC62828)
private val idLocale = Locale("id", "ID")

private fun dayText(millis: Long): String =
    SimpleDateFormat("EEEE, d MMM yyyy", idLocale).format(Date(millis))

private fun createdText(millis: Long): String =
    if (millis <= 0) "" else "Dikirim " + SimpleDateFormat("d MMM, HH.mm", idLocale).format(Date(millis))

private fun dayKey(millis: Long): String = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(millis))

private fun statusColors(status: PickupStatus): Pair<Color, Color> = when (status) {
    PickupStatus.MENUNGGU -> Color(0xFFE66A00) to Color(0xFFFFEBD6)
    PickupStatus.DITERIMA -> Color(0xFF2E7D32) to Color(0xFFE4F1D4)
    PickupStatus.DITOLAK -> Danger to Color(0xFFFFEDEA)
    PickupStatus.SELESAI -> Color(0xFF1E7FD1) to Color(0xFFE1EFFB)
    PickupStatus.DIBATALKAN -> Color(0xFF616161) to Color(0xFFEEEEEE)
}

/** Urutan tampil di filter "Semua": yang perlu ditindak paling atas. */
private fun PickupStatus.priority(): Int = when (this) {
    PickupStatus.MENUNGGU -> 0
    PickupStatus.DITERIMA -> 1
    PickupStatus.SELESAI -> 2
    PickupStatus.DITOLAK -> 3
    PickupStatus.DIBATALKAN -> 4
}

private fun openMap(context: Context, r: PickupRequest) {
    val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${r.latitude},${r.longitude}")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak ada aplikasi peta di HP ini", Toast.LENGTH_SHORT).show()
    }
}

private fun callPhone(context: Context, phone: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Tidak bisa membuka telepon", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val items by viewModel.requests.collectAsState()
    val error by viewModel.error.collectAsState()
    val loaded by viewModel.loaded.collectAsState()

    var tab by rememberSaveable { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf<String?>(PickupStatus.MENUNGGU.name) }
    var approveTarget by remember { mutableStateOf<PickupRequest?>(null) }
    var rejectTarget by remember { mutableStateOf<PickupRequest?>(null) }
    var showLogout by remember { mutableStateOf(false) }

    val shown = items
        .filter { filter == null || it.status.name == filter }
        .sortedWith(compareBy<PickupRequest>({ it.status.priority() }, { it.dayMillis }, { it.createdMillis }))

    val todayKey = dayKey(System.currentTimeMillis())
    val jemputHariIni = items.count { it.status == PickupStatus.DITERIMA && dayKey(it.dayMillis) == todayKey }

    fun apply(id: String, status: PickupStatus, time: String, note: String, successMsg: String) {
        viewModel.update(id, status, time, note) { err ->
            Toast.makeText(context, err ?: successMsg, Toast.LENGTH_SHORT).show()
        }
    }

    ScreenColumn(title = "Panel Admin", onBack = null) {
        // ===== Kartu sapaan + ringkasan =====
        AdminHero(
            icon = Icons.Rounded.AdminPanelSettings,
            title = "Halo, Admin",
            subtitle = SimpleDateFormat("EEEE, d MMMM yyyy", idLocale).format(Date()),
            modifier = Modifier.reveal(0),
            actions = {
                HeroIconButton(Icons.AutoMirrored.Rounded.Logout, "Keluar") { showLogout = true }
            }
        ) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    PickupStatus.MENUNGGU to "Menunggu",
                    PickupStatus.DITERIMA to "Disetujui",
                    PickupStatus.SELESAI to "Selesai"
                ).forEach { (status, label) ->
                    HeroStat(
                        value = "${items.count { it.status == status }}",
                        label = label,
                        selected = tab == 0 && filter == status.name,
                        onClick = { tab = 0; filter = status.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (jemputHariIni > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Lime)
                        .clickable { tab = 0; filter = PickupStatus.DITERIMA.name }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.LocalShipping, null, tint = GreenDark, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$jemputHariIni penjemputan dijadwalkan hari ini",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenDark
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        AdminTabSwitch(selected = tab, onSelect = { tab = it }, modifier = Modifier.reveal(1))
        Spacer(Modifier.height(16.dp))

        if (tab == 1) {
            AdminPriceSection()
            Spacer(Modifier.height(20.dp))
        } else {
            // ===== Filter status =====
            Text(
                "Request jemput", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
                modifier = Modifier.reveal(1)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).reveal(1),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    PickupStatus.MENUNGGU to "Menunggu",
                    PickupStatus.DITERIMA to "Disetujui",
                    PickupStatus.SELESAI to "Selesai",
                    PickupStatus.DITOLAK to "Ditolak",
                    PickupStatus.DIBATALKAN to "Dibatalkan"
                ).forEach { (status, label) ->
                    val (fg, bg) = statusColors(status)
                    AdminFilterChip(
                        label = label,
                        count = items.count { it.status == status },
                        selected = filter == status.name,
                        fg = fg,
                        bg = bg,
                        onClick = { filter = status.name }
                    )
                }
                AdminFilterChip(
                    label = "Semua",
                    count = items.size,
                    selected = filter == null,
                    fg = GreenDark,
                    bg = Lime,
                    onClick = { filter = null }
                )
            }

            Spacer(Modifier.height(14.dp))

            // ===== Daftar =====
            when {
                !loaded -> InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = GreenPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Memuat request…", fontSize = 13.sp, color = TextDark)
                    }
                }

                error != null && items.isEmpty() -> AdminEmptyState(
                    icon = Icons.Rounded.ErrorOutline,
                    title = "Gagal memuat data",
                    message = error ?: "",
                    accent = Danger,
                    actionLabel = "Coba lagi",
                    onAction = { viewModel.refresh() }
                )

                shown.isEmpty() -> AdminEmptyState(
                    icon = if (filter == PickupStatus.MENUNGGU.name) Icons.Rounded.CheckCircle else Icons.Rounded.LocalShipping,
                    title = if (filter == PickupStatus.MENUNGGU.name) "Semua request sudah ditangani" else "Belum ada request",
                    message = "Request baru dari pengguna akan muncul di sini secara otomatis."
                )

                else -> shown.forEachIndexed { i, request ->
                    AdminCard(
                        request = request,
                        modifier = Modifier.reveal(2 + i.coerceAtMost(6)),
                        onApprove = { approveTarget = request },
                        onReject = { rejectTarget = request },
                        onFinish = {
                            apply(request.id, PickupStatus.SELESAI, request.slot, request.adminNote, "Ditandai selesai")
                        },
                        onMap = { openMap(context, request) },
                        onCall = if (request.requesterPhone.isNotBlank()) ({ callPhone(context, request.requesterPhone) }) else null
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    approveTarget?.let { target ->
        TimeDialog(
            title = if (target.status == PickupStatus.DITERIMA) "Ubah jam penjemputan" else "Setujui request",
            subtitle = "Tentukan jam penjemputan pada ${dayText(target.dayMillis)}.",
            initial = target.slot,
            onDismiss = { approveTarget = null },
            onConfirm = { time ->
                approveTarget = null
                apply(target.id, PickupStatus.DITERIMA, time, "", "Request disetujui")
            }
        )
    }

    rejectTarget?.let { target ->
        ReasonDialog(
            onDismiss = { rejectTarget = null },
            onConfirm = { reason ->
                rejectTarget = null
                apply(target.id, PickupStatus.DITOLAK, "", reason, "Request ditolak")
            }
        )
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor = Color.White,
            title = { Text("Keluar dari akun admin?", fontWeight = FontWeight.Bold) },
            text = { Text("Anda perlu login lagi untuk mengelola request.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    onLogout()
                }) { Text("Keluar", color = Danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Batal", color = TextMuted) }
            }
        )
    }
}

@Composable
private fun AdminCard(
    request: PickupRequest,
    modifier: Modifier = Modifier,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onFinish: () -> Unit,
    onMap: () -> Unit,
    onCall: (() -> Unit)?
) {
    val (fg, bg) = statusColors(request.status)
    val name = request.requesterName.ifBlank { "Pengguna" }

    InfoCard(modifier = modifier) {
        // Pemohon + status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(ChipBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.trim().take(1).uppercase(),
                    fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = GreenDark
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                val dikirim = createdText(request.createdMillis)
                if (dikirim.isNotBlank()) Text(dikirim, fontSize = 11.sp, color = TextMuted)
            }
            Spacer(Modifier.width(8.dp))
            StatusPill(request.status.label, fg, bg)
        }

        Spacer(Modifier.height(12.dp))

        // Jadwal + alamat dalam kotak lembut
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SplashBackground)
                .padding(12.dp)
        ) {
            InfoRow(Icons.Rounded.Schedule) {
                Text(dayText(request.dayMillis), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(
                    if (request.slot.isNotBlank()) "Pukul ${request.slot}" else "Jam belum ditetapkan",
                    fontSize = 12.sp,
                    fontWeight = if (request.slot.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                    color = if (request.slot.isNotBlank()) GreenPrimary else TextMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            InfoRow(Icons.Rounded.LocationOn) {
                Text(request.address, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark, lineHeight = 18.sp)
                if (request.detail.isNotBlank()) {
                    Text(request.detail, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Jenis sampah + tombol peta / telepon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                request.types.forEach { CategoryChip(it) }
            }
            if (onCall != null) {
                SmallAction(Icons.Rounded.Phone, "Telepon", onCall)
                Spacer(Modifier.width(6.dp))
            }
            SmallAction(Icons.Rounded.Map, "Buka peta", onMap)
        }

        if (request.note.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            NoteBox("Catatan pengguna", request.note, TextDark, ChipBg)
        }
        if (request.adminNote.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            val ditolak = request.status == PickupStatus.DITOLAK
            NoteBox(
                if (ditolak) "Alasan ditolak" else "Catatan admin",
                request.adminNote,
                if (ditolak) Danger else TextDark,
                if (ditolak) Color(0xFFFFEDEA) else Color(0xFFF1F4EE)
            )
        }

        when (request.status) {
            PickupStatus.MENUNGGU -> {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Danger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger),
                        modifier = Modifier.weight(1f)
                    ) { Text("Tolak", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark),
                        modifier = Modifier.weight(1f)
                    ) { Text("Setujui", fontWeight = FontWeight.Bold) }
                }
            }

            PickupStatus.DITERIMA -> {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onApprove,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, ChipBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                        modifier = Modifier.weight(1f)
                    ) { Text("Ubah jam", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = onFinish,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) { Text("Tandai selesai", fontWeight = FontWeight.Bold) }
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GreenPrimary, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun SmallAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(ChipBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = GreenDark, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun NoteBox(label: String, text: String, color: Color, background: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f))
        Spacer(Modifier.height(2.dp))
        Text(text, fontSize = 13.sp, color = color, lineHeight = 18.sp)
    }
}

@Composable
private fun TimeDialog(
    title: String,
    subtitle: String,
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initial.split(".", ":")
    var hourText by remember { mutableStateOf(parts.getOrNull(0)?.takeIf { initial.isNotBlank() } ?: "") }
    var minuteText by remember { mutableStateOf(parts.getOrNull(1)?.takeIf { initial.isNotBlank() } ?: "") }
    var showError by remember { mutableStateOf(false) }

    val hour = hourText.toIntOrNull()
    val minute = minuteText.toIntOrNull()
    val valid = hour != null && minute != null && hour in 0..23 && minute in 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(subtitle, fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DialogField(
                        value = hourText,
                        onValueChange = { hourText = it.filter { c -> c.isDigit() }.take(2) },
                        label = "Jam",
                        number = true,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = ":",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    DialogField(
                        value = minuteText,
                        onValueChange = { minuteText = it.filter { c -> c.isDigit() }.take(2) },
                        label = "Menit",
                        number = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(10.dp))
                // Pilihan cepat
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("08.00", "09.00", "10.00", "13.00", "15.00").forEach { j ->
                        Text(
                            j,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenDark,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(ChipBg)
                                .clickable {
                                    hourText = j.substringBefore(".")
                                    minuteText = j.substringAfter(".")
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                if (showError && !valid) {
                    Text(
                        text = "Jam 0–23 dan menit 0–59.",
                        fontSize = 12.sp,
                        color = Danger,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (valid) onConfirm("%02d.%02d".format(hour, minute)) else showError = true
            }) { Text("Simpan", color = GreenPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted) }
        }
    )
}

@Composable
private fun ReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Tolak request", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Tulis alasan agar pengguna tahu kenapa ditolak.", fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(12.dp))
                DialogField(
                    value = reason,
                    onValueChange = { reason = it.take(255) },
                    label = "Alasan penolakan",
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                // Alasan yang sering dipakai
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Di luar wilayah layanan", "Jadwal penuh", "Jenis sampah tidak diterima").forEach { a ->
                        Text(
                            a,
                            fontSize = 12.sp,
                            color = TextDark,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFFFEDEA))
                                .clickable { reason = a }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                if (showError && reason.isBlank()) {
                    Text(
                        text = "Alasan wajib diisi.",
                        fontSize = 12.sp,
                        color = Danger,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (reason.isNotBlank()) onConfirm(reason.trim()) else showError = true
            }) { Text("Tolak", color = Danger, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted) }
        }
    )
}

@Composable
private fun DialogField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    number: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (number) KeyboardType.Number else KeyboardType.Text
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = ChipBorder,
            focusedLabelColor = GreenPrimary,
            cursorColor = GreenPrimary,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark
        ),
        modifier = modifier
    )
}