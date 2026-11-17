package com.example.pilahin.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted

enum class AlertType(val color: Color, val container: Color, val icon: ImageVector) {
    SUCCESS(Color(0xFF2E7D32), Color(0xFFE4F1D4), Icons.Rounded.CheckCircle),
    ERROR(Color(0xFFC62828), Color(0xFFFFEDEA), Icons.Rounded.Error),
    WARNING(Color(0xFFE66A00), Color(0xFFFFEBD6), Icons.Rounded.Warning),
    INFO(Color(0xFF1E7FD1), Color(0xFFE1EFFB), Icons.Rounded.Info)
}

data class AlertData(
    val type: AlertType,
    val title: String,
    val message: String,
    val primaryText: String = "Mengerti",
    val onPrimary: () -> Unit = {},
    val secondaryText: String? = null,
    val onSecondary: () -> Unit = {},
    val icon: ImageVector? = null,
    /** false = tidak bisa ditutup dengan ketuk luar / tombol back */
    val cancelable: Boolean = true
)

@Composable
fun AppAlert(data: AlertData, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = { if (data.cancelable) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = data.cancelable,
            dismissOnClickOutside = data.cancelable
        )
    ) {
        val progress = remember { Animatable(0.6f) }
        LaunchedEffect(Unit) {
            progress.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = progress.value
                    scaleY = progress.value
                    alpha = ((progress.value - 0.6f) / 0.4f).coerceIn(0f, 1f)
                }
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ikon dengan lingkaran berlapis
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(data.type.container.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(data.type.container),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon ?: data.type.icon,
                        contentDescription = null,
                        tint = data.type.color,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = data.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = data.message,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
            val soft = data.type == AlertType.SUCCESS || data.type == AlertType.INFO
            Button(
                onClick = {
                    onDismiss()
                    data.onPrimary()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (soft) Lime else data.type.color,
                    contentColor = if (soft) TextDark else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(data.primaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            if (data.secondaryText != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        onDismiss()
                        data.onSecondary()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        data.secondaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
        }
    }
}