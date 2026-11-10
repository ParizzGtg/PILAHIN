package com.example.pilahin.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pilahin.data.ScanResult
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.scan.ScanViewModel
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: ScanViewModel,
    onOpen: (ScanResult) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var filter by rememberSaveable { mutableStateOf<String?>(null) }

    val items = state.history.filter { filter == null || it.category.name == filter }
    val formatter = remember { SimpleDateFormat("d MMMM yyyy", Locale("id", "ID")) }
    val grouped = items.groupBy { formatter.format(Date(it.timeMillis)) }

    ScreenColumn(title = "Riwayat Klasifikasi", onBack = onBack) {
        Reveal(0) {
            Text(
                text = "Langkah kecilmu tercatat di sini. Lihat kembali dan pelajari cara memilah sampah.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .reveal(1),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem("Semua", filter == null) { filter = null }
            WasteCategory.entries.forEach {
                FilterChipItem(it.label, filter == it.name) { filter = it.name }
            }
        }
        Spacer(Modifier.height(14.dp))

        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
                    .reveal(2),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Belum ada riwayat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Scan sampah lalu tekan \"Simpan ke Riwayat\" agar hasilnya muncul di sini.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            var n = 2
            grouped.forEach { (date, list) ->
                Text(
                    text = date,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.reveal(minOf(n++, 12))
                )
                Spacer(Modifier.height(8.dp))
                list.forEach { item ->
                    InfoCard(
                        modifier = Modifier
                            .reveal(minOf(n++, 12))
                            .clickable { onOpen(item) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = File(item.imagePath),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE4F1D4))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(
                                    "${(item.confidence * 100).toInt()}% akurat",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Spacer(Modifier.height(4.dp))
                                CategoryChip(item.category)
                            }
                            Icon(Icons.Rounded.ChevronRight, null, tint = TextDark)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(6.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) GreenDark else TextDark,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Lime else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}