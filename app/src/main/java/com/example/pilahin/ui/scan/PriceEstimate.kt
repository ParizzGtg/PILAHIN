package com.example.pilahin.ui.scan

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.PriceStore
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.data.rupiah
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun PriceCard(name: String, category: WasteCategory, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prices by PriceStore.prices.collectAsState()
    LaunchedEffect(Unit) { PriceStore.refresh(context) }

    val matches = remember(name, prices) { PriceStore.match(name, prices) }

    InfoCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(ChipBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Payments, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Perkiraan nilai jual", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("Harga beli UPT Bank Sampah", fontSize = 12.sp, color = TextMuted)
            }
        }

        if (matches.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = when (category) {
                    WasteCategory.ANORGANIK ->
                        "Benda ini belum ada di daftar harga. Tanyakan ke bank sampah terdekat."
                    WasteCategory.ORGANIK ->
                        "Tidak ada harga jual, tetapi bisa diolah menjadi kompos untuk tanaman."
                    WasteCategory.B3 ->
                        "Tidak untuk dijual. Serahkan ke titik pengumpulan B3 agar aman."
                    WasteCategory.RESIDU ->
                        "Tidak memiliki nilai jual karena tidak bisa didaur ulang."
                },
                fontSize = 13.sp,
                color = TextDark,
                lineHeight = 18.sp
            )
        } else {
            val sameUnit = matches.map { it.unitLabel }.distinct().size == 1
            val min = matches.minOf { it.price }
            val max = matches.maxOf { it.price }

            if (sameUnit) {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ChipBg)
                        .padding(12.dp)
                ) {
                    Text("Kisaran per ${matches.first().unitLabel}", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = if (min == max) rupiah(min) else "${rupiah(min)} – ${rupiah(max)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenDark
                    )
                    Text(
                        text = "Harga tergantung kondisi barang (bersih atau kotor, ada label atau tidak).",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            matches.forEachIndexed { i, p ->
                if (i > 0) HorizontalDivider(color = ChipBorder)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        if (p.example.isNotBlank()) {
                            Text(p.example, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(rupiah(p.price), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = GreenDark)
                        Text("per ${p.unitLabel}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = "Berlaku mulai Oktober 2025. Harga dapat berubah sewaktu-waktu sesuai harga pasar.",
            fontSize = 10.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )
    }
}