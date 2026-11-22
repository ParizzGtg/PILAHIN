package com.example.pilahin.ui.admin

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.data.PickupStatus
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Danger = Color(0xFFC62828)
private val idLocale = Locale("id", "ID")

private fun dayText(millis: Long): String =
    SimpleDateFormat("EEEE, d MMM yyyy", idLocale).format(Date(millis))

private fun statusColors(status: PickupStatus): Pair<Color, Color> = when (status) {
    PickupStatus.MENUNGGU -> Color(0xFFE66A00) to Color(0xFFFFEBD6)
    PickupStatus.DITERIMA -> Color(0xFF2E7D32) to Color(0xFFE4F1D4)
    PickupStatus.DITOLAK -> Danger to Color(0xFFFFEDEA)
    PickupStatus.SELESAI -> Color(0xFF1E7FD1) to Color(0xFFE1EFFB)
    PickupStatus.DIBATALKAN -> Color(0xFF616161) to Color(0xFFEEEEEE)
}

@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val items by viewModel.requests.collectAsState()
    val error by viewModel.error.collectAsState()

    var filter by rememberSaveable { mutableStateOf<String?>(PickupStatus.MENUNGGU.name) }
    var approveTarget by remember { mutableStateOf<PickupRequest?>(null) }
    var rejectTarget by remember { mutableStateOf<PickupRequest?>(null) }
    var showLogout by remember { mutableStateOf(false) }

    val shown = items
        .filter { filter == null || it.status.name == filter }
        .sortedBy { it.dayMillis }

    fun apply(id: String, status: PickupStatus, time: String, note: String, successMsg: String) {
        viewModel.update(id, status, time, note) { err ->
            Toast.makeText(context, err ?: successMsg, Toast.LENGTH_SHORT).show()
        }
    }

    ScreenColumn(title = "Panel Admin", onBack = null) {
        // Info admin + keluar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(0),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Kelola request jemput", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("${items.count { it.status == PickupStatus.MENUNGGU }} menunggu konfirmasi", fontSize = 12.sp, color = TextMuted)
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFEDEA))
                    .clickable { showLogout = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, "Keluar", tint = Danger, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        // Filter status
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                PickupStatus.MENUNGGU.name to "Menunggu",
                PickupStatus.DITERIMA.name to "Disetujui",
                PickupStatus.DITOLAK.name to "Ditolak",
                PickupStatus.SELESAI.name to "Selesai",
                null to "Semua"
            )
            tabs.forEach { (key, label) ->
                val count = if (key == null) items.size else items.count { it.status.name == key }
                val selected = filter == key
                Text(
                    text = "$label ($count)",
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) GreenDark else TextDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) Lime else Color.White)
                        .clickable { filter = key }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        if (error != null && items.isEmpty()) {
            InfoCard {
                Text("Gagal memuat data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Danger)
                Text(error ?: "", fontSize = 12.sp, color = TextMuted)
            }
            Spacer(Modifier.height(10.dp))
        } else if (shown.isEmpty()) {
            InfoCard {
                Text("Tidak ada request", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Request baru dari pengguna akan muncul di sini.", fontSize = 12.sp, color = TextMuted)
            }
            Spacer(Modifier.height(10.dp))
        } else {
            shown.forEach { request ->
                AdminCard(
                    request = request,
                    onApprove = { approveTarget = request },
                    onReject = { rejectTarget = request },
                    onFinish = {
                        apply(request.id, PickupStatus.SELESAI, request.slot, request.adminNote, "Ditandai selesai")
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
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
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onFinish: () -> Unit
) {
    val (fg, bg) = statusColors(request.status)
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
        }
        Spacer(Modifier.height(10.dp))

        if (request.requesterName.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(request.requesterName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            Spacer(Modifier.height(6.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Schedule, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            val jam = if (request.slot.isNotBlank()) " • pukul ${request.slot}" else ""
            Text("${dayText(request.dayMillis)}$jam", fontSize = 13.sp, color = TextDark)
        }
        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.LocationOn, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(request.address, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                if (request.detail.isNotBlank()) {
                    Text(request.detail, fontSize = 11.sp, color = TextMuted)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            request.types.forEach { CategoryChip(it) }
        }

        if (request.note.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("Catatan user: ${request.note}", fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
        }
        if (request.adminNote.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text("Catatan admin: ${request.adminNote}", fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
        }

        when (request.status) {
            PickupStatus.MENUNGGU -> {
                Spacer(Modifier.height(12.dp))
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
                Spacer(Modifier.height(12.dp))
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
                        colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark),
                        modifier = Modifier.weight(1f)
                    ) { Text("Tandai selesai", fontWeight = FontWeight.Bold) }
                }
            }

            else -> Unit
        }
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
                    onValueChange = { reason = it },
                    label = "Alasan penolakan",
                    modifier = Modifier.fillMaxWidth()
                )
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