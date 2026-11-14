package com.example.pilahin.ui.pickup

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.ui.common.InfoCard
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

@Composable
fun NotificationsScreen(
    viewModel: PickupViewModel,
    onBack: () -> Unit
) {
    val items by viewModel.notifications.collectAsState()

    // Yang belum dibaca tetap ditandai selama layar terbuka, lalu otomatis dibaca saat keluar
    DisposableEffect(Unit) {
        onDispose { viewModel.markAllRead() }
    }

    ScreenColumn(title = "Notifikasi", onBack = onBack) {
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp)
                    .reveal(0),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Belum ada notifikasi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Kabar tentang request jemput dan pengingat akan muncul di sini.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .reveal(0),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.clearNotifications() }) {
                    Text("Hapus semua", color = GreenPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
            items.forEachIndexed { index, item ->
                InfoCard(modifier = Modifier.reveal(minOf(index + 1, 8))) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (item.read) TextMuted.copy(alpha = 0.25f) else Lime)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = if (item.read) FontWeight.SemiBold else FontWeight.Bold,
                                color = TextDark
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(item.message, fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(formatDateTime(item.timeMillis), fontSize = 11.sp, color = TextMuted)
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.deleteNotification(item.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Close, "Hapus", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}