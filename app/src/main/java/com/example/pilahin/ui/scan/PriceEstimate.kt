package com.example.pilahin.ui.scan

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
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

data class PriceEstimate(
    val material: String,
    val perKg: String?,
    val perItem: String?,
    val note: String
)

fun estimatePrice(name: String, category: WasteCategory): PriceEstimate {
    val n = name.lowercase()
    return when {
        "kaca" in n || "glass" in n -> PriceEstimate(
            "Kaca / botol kaca", "Rp 300 – 1.000", "± Rp 100 – 500 per botol utuh",
            "Nilainya rendah dan tidak semua bank sampah menerima. Jaga agar tidak pecah."
        )
        "kaleng" in n || "can" in n || "aluminium" in n || "aluminum" in n -> PriceEstimate(
            "Kaleng aluminium", "Rp 10.000 – 15.000", "± Rp 100 – 250 per kaleng",
            "Pipihkan dan bilas. Kaleng besi (makanan) harganya jauh lebih rendah."
        )
        "botol" in n || "bottle" in n || "plastic" in n || "plastik" in n -> PriceEstimate(
            "Plastik PET (botol bekas)", "Rp 3.000 – 5.000", "± Rp 50 – 150 per botol",
            "Lepas tutup dan label, lalu bilas bersih agar harga lebih tinggi."
        )
        "kardus" in n || "cardboard" in n -> PriceEstimate(
            "Kardus", "Rp 1.500 – 2.500", null,
            "Jaga tetap kering. Kardus basah atau berminyak dihargai lebih rendah."
        )
        "kertas" in n || "paper" in n || "koran" in n -> PriceEstimate(
            "Kertas", "Rp 1.000 – 2.500", null,
            "Pisahkan dari kertas berminyak atau berlapis plastik."
        )
        "besi" in n || "logam" in n || "metal" in n -> PriceEstimate(
            "Besi / logam", "Rp 3.000 – 6.000", null,
            "Harga naik-turun mengikuti pasar logam."
        )
        else -> when (category) {
            WasteCategory.ANORGANIK -> PriceEstimate(
                "Anorganik", null, null,
                "Harga tergantung jenis bahannya. Tanyakan ke bank sampah terdekat."
            )
            WasteCategory.ORGANIK -> PriceEstimate(
                "Organik", null, null,
                "Tidak ada harga jual, tetapi bisa diolah menjadi kompos untuk tanaman."
            )
            WasteCategory.B3 -> PriceEstimate(
                "B3", null, null,
                "Tidak untuk dijual. Serahkan ke titik pengumpulan B3 agar aman."
            )
            WasteCategory.RESIDU -> PriceEstimate(
                "Residu", null, null,
                "Tidak memiliki nilai jual karena tidak bisa didaur ulang."
            )
        }
    }
}

@Composable
fun PriceCard(name: String, category: WasteCategory, modifier: Modifier = Modifier) {
    val price = estimatePrice(name, category)
    InfoCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(ChipBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Payments, null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Perkiraan nilai jual", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(price.material, fontSize = 12.sp, color = TextMuted)
            }
        }

        if (price.perKg != null) {
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChipBg)
                    .padding(12.dp)
            ) {
                Text("Per kilogram", fontSize = 11.sp, color = TextMuted)
                Text(price.perKg, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GreenDark)
                if (price.perItem != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(price.perItem, fontSize = 12.sp, color = TextDark)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(price.note, fontSize = 12.sp, color = TextDark, lineHeight = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Perkiraan harga pengepul dan bank sampah. Harga sebenarnya berbeda tiap daerah dan berubah-ubah.",
            fontSize = 10.sp,
            color = TextMuted,
            lineHeight = 14.sp
        )
    }
}