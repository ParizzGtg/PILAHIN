package com.example.pilahin.ui.menu

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.Reveal
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.BlobGreen
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun OilScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    ScreenColumn(title = "Minyak Jelantah", onBack = onBack) {
        Reveal(0) {
            Column {
                Text(
                    text = "KEBIASAAN BAIK DARI DAPUR",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GreenDark
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sisa minyak, masih bermanfaat.",
                        fontSize = 24.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BlobGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.WaterDrop, null, tint = GreenPrimary, modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        InfoCard(modifier = Modifier.reveal(1)) {
            Text("Simpan dengan aman", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(8.dp))
            listOf(
                "Dinginkan dan saring" to "Tunggu minyak dingin, lalu saring sisa remah makanan.",
                "Tutup, label, simpan wadah" to "Gunakan botol tertutup rapat dan beri label \"minyak jelantah\".",
                "Jauhkan dari panas" to "Simpan di tempat sejuk dan jauh dari jangkauan anak."
            ).forEachIndexed { i, (title, desc) ->
                Row(modifier = Modifier.padding(vertical = 5.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Lime),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text(desc, fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .reveal(2)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF4E0))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Warning, null, tint = Color(0xFFE66A00), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Jangan buang ke saluran air atau tanah. Minyak dapat menyumbat pipa dan mencemari lingkungan.",
                fontSize = 12.sp,
                color = TextDark,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        InfoCard(modifier = Modifier.reveal(3)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lightbulb, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Jadi sabun atau lilin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Jelantah yang sudah disaring bisa diolah menjadi sabun, lilin, atau diserahkan ke pengepul untuk diolah kembali.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        Reveal(4) {
            Button(
                onClick = {
                    Toast.makeText(context, "Peta pengolah jelantah segera hadir", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text("Temukan Pengolah Jelantah", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Rounded.LocationOn, null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}