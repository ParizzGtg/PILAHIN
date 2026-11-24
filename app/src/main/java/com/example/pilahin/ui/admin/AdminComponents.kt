package com.example.pilahin.ui.admin

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

/** Kartu sapaan hijau di atas panel admin. */
@Composable
fun AdminHero(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(GreenDark)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Lime),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GreenDark, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
            }
            actions()
        }
        content()
    }
}

@Composable
fun HeroIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun HeroStat(
    value: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Lime else Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (selected) GreenDark else Color.White
        )
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) GreenDark else Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun AdminFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    fg: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Text(
        text = "$label ($count)",
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) fg else TextDark,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) bg else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
fun StatusPill(text: String, fg: Color, bg: Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun AdminEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    accent: Color = GreenPrimary,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    InfoCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(message, fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center, lineHeight = 17.sp)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark)
                ) { Text(actionLabel, fontWeight = FontWeight.Bold) }
            }
        }
    }
}