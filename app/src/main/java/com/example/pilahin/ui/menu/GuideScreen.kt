package com.example.pilahin.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.color
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun GuideScreen(onBack: () -> Unit, onScan: () -> Unit) {
    ScreenColumn(title = "Panduan Pemilahan", onBack = onBack) {
        Reveal(0) {
            Column {
                Text(
                    text = "Kenali jenis, pilah tepat.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Empat kategori utama sampah rumah tangga.",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        WasteCategory.entries.forEachIndexed { i, category ->
            InfoCard(modifier = Modifier.reveal(i + 1)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (category) {
                            WasteCategory.ORGANIK -> Icons.Rounded.Eco
                            WasteCategory.ANORGANIK -> Icons.Rounded.Recycling
                            WasteCategory.B3 -> Icons.Rounded.Science
                            WasteCategory.RESIDU -> Icons.Rounded.Delete
                        },
                        contentDescription = null,
                        tint = category.color(),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    CategoryChip(category)
                }
                Spacer(Modifier.height(8.dp))
                Text(category.description, fontSize = 13.sp, color = TextDark, lineHeight = 19.sp)
                Spacer(Modifier.height(6.dp))
                Text("Contoh: ${category.example}", fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
                Spacer(Modifier.height(2.dp))
                Text("Tips: ${category.tip}", fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
            }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(4.dp))
        Reveal(WasteCategory.entries.size + 1) {
            Button(
                onClick = onScan,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Masih ragu? Scan Sampah", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.DocumentScanner, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}