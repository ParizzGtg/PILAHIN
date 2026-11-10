package com.example.pilahin.ui.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pilahin.ui.auth.PrimaryButton
import com.example.pilahin.ui.common.CategoryChip
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.color
import com.example.pilahin.ui.common.container
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.io.File

@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    onBack: () -> Unit,
    onRescan: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val item = state.current
    if (item == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    ScreenColumn(title = "Hasil Klasifikasi", onBack = onBack) {
        AsyncImage(
            model = File(item.imagePath),
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .reveal(0)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFE4F1D4))
        )

        Spacer(Modifier.height(14.dp))

        Reveal(1) {
            Column {
                Text(
                    text = item.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(item.category)
                    Text(
                        text = "Akurasi ${(item.confidence * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenDark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFEAF6DD))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Jenis sampah: ${item.category.label}. ${item.category.description}",
                    fontSize = 13.sp,
                    color = TextMuted,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        InfoCard(modifier = Modifier.reveal(2)) {
            Text(
                text = "Cara memilah yang tepat",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(Modifier.height(10.dp))
            item.category.steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(item.category.container()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.category.color()
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Text(text = step, fontSize = 13.sp, color = TextDark)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Reveal(3) {
            Text(
                text = "Hasil ini berupa perkiraan AI. Jika ragu, periksa kembali lewat Panduan Pemilahan.",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Reveal(4) {
            PrimaryButton(
                text = if (state.saved) "Tersimpan di Riwayat ✓" else "Simpan ke Riwayat",
                onClick = { viewModel.saveCurrent() }
            )
        }
        Spacer(Modifier.height(10.dp))
        Reveal(5) {
            OutlinedButton(
                onClick = onRescan,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, ChipBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = TextDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Scan ulang", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Icon(Icons.Rounded.DocumentScanner, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(20.dp).navigationBarsPadding())
    }
}