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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.PriceStore
import com.example.pilahin.data.WastePrice
import com.example.pilahin.data.rupiah
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import kotlinx.coroutines.launch

/** Pengalih tab Request / Harga. */
@Composable
fun AdminTabSwitch(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(4.dp)
    ) {
        listOf("Request Jemput", "Harga Sampah").forEachIndexed { i, label ->
            val on = selected == i
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (on) Lime else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (on) GreenDark else TextMuted
                )
            }
        }
    }
}

private fun groupColors(group: String): Pair<Color, Color> = when (group) {
    "Plastik" -> Color(0xFF1E7FD1) to Color(0xFFE1EFFB)
    "Logam" -> Color(0xFF546E7A) to Color(0xFFECEFF1)
    "Kertas" -> Color(0xFFB26A00) to Color(0xFFFFF1D6)
    "Botol Kaca" -> Color(0xFF00897B) to Color(0xFFDDF3F0)
    "Minyak Jelantah" -> Color(0xFFE66A00) to Color(0xFFFFEBD6)
    else -> GreenDark to ChipBg
}

@Composable
fun AdminPriceSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prices by PriceStore.prices.collectAsState()
    val fromServer by PriceStore.fromServer.collectAsState()

    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<String?>(null) }
    var editTarget by remember { mutableStateOf<WastePrice?>(null) }

    LaunchedEffect(Unit) { PriceStore.refresh(context) }

    val groups = prices.map { it.group }.distinct()
    val shown = prices.filter { p ->
        (group == null || p.group == group) &&
                (query.isBlank() ||
                        p.name.contains(query, true) ||
                        p.example.contains(query, true) ||
                        p.code.contains(query, true))
    }

    Column {
        if (!fromServer) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF4E0))
                    .clickable { scope.launch { PriceStore.refresh(context) } }
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Rounded.Info, null, tint = Color(0xFFE66A00), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Menampilkan data bawaan. Harga belum bisa diubah karena belum tersambung ke server. Ketuk untuk muat ulang.",
                    fontSize = 12.sp, color = TextDark, lineHeight = 17.sp
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Pencarian
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Cari barang atau kode") },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = ChipBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = GreenPrimary,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf<String?>(null).plus(groups).forEach { g ->
                val on = group == g
                Text(
                    text = (g ?: "Semua") + " (" + (if (g == null) prices.size else prices.count { it.group == g }) + ")",
                    fontSize = 12.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                    color = if (on) GreenDark else TextDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (on) Lime else Color.White)
                        .clickable { group = g }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (shown.isEmpty()) {
            InfoCard {
                Text("Tidak ada barang yang cocok", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Coba kata kunci lain.", fontSize = 12.sp, color = TextMuted)
            }
        } else {
            shown.forEach { p ->
                val (fg, bg) = groupColors(p.group)
                InfoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    p.code,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fg,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(bg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(p.group, fontSize = 11.sp, color = TextMuted)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(p.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            if (p.example.isNotBlank()) {
                                Text(p.example, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(rupiah(p.price), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GreenDark)
                            Text("per ${p.unitLabel}", fontSize = 10.sp, color = TextMuted)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (fromServer) Lime else Color(0xFFEEEEEE))
                                .clickable {
                                    if (fromServer) editTarget = p
                                    else Toast.makeText(
                                        context, "Belum tersambung ke server", Toast.LENGTH_SHORT
                                    ).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Edit, "Ubah harga",
                                tint = if (fromServer) GreenDark else TextMuted,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        Text(
            "Berlaku mulai Oktober 2025. Perubahan harga langsung terlihat oleh semua pengguna.",
            fontSize = 11.sp, color = TextMuted
        )
    }

    editTarget?.let { target ->
        var text by remember(target.id) { mutableStateOf(target.price.toString()) }
        var showError by remember(target.id) { mutableStateOf(false) }
        val value = text.toIntOrNull()
        val valid = value != null && value in 0..10_000_000

        AlertDialog(
            onDismissRequest = { editTarget = null },
            containerColor = Color.White,
            title = { Text("Ubah harga", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(target.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text("Harga saat ini ${rupiah(target.price)} per ${target.unitLabel}", fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it.filter { c -> c.isDigit() }.take(8) },
                        label = { Text("Harga baru (Rp per ${target.unitLabel})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = ChipBorder,
                            focusedLabelColor = GreenPrimary,
                            cursorColor = GreenPrimary,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showError && !valid) {
                        Text("Masukkan angka yang valid.", fontSize = 12.sp, color = Color(0xFFC62828), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (!valid) { showError = true; return@TextButton }
                    val newPrice = value!!
                    editTarget = null
                    scope.launch {
                        val err = PriceStore.update(context, target.id, newPrice)
                        Toast.makeText(context, err ?: "Harga diperbarui", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Simpan", color = GreenPrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) { Text("Batal", color = TextMuted) }
            }
        )
    }
}