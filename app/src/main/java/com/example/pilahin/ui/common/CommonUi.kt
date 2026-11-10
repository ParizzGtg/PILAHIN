package com.example.pilahin.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextDark

val CardBorderStroke = BorderStroke(1.dp, Color(0xFFE3EDD9))

fun WasteCategory.color(): Color = when (this) {
    WasteCategory.ORGANIK -> Color(0xFF2E7D32)
    WasteCategory.ANORGANIK -> Color(0xFF1E7FD1)
    WasteCategory.B3 -> Color(0xFFE66A00)
    WasteCategory.RESIDU -> Color(0xFF7B5EA7)
}

fun WasteCategory.container(): Color = when (this) {
    WasteCategory.ORGANIK -> Color(0xFFE4F1D4)
    WasteCategory.ANORGANIK -> Color(0xFFE1EFFB)
    WasteCategory.B3 -> Color(0xFFFFEBD6)
    WasteCategory.RESIDU -> Color(0xFFECE6F6)
}

@Composable
fun CategoryChip(category: WasteCategory, modifier: Modifier = Modifier) {
    Text(
        text = category.label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = category.color(),
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(category.container())
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = CardBorderStroke,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun Modifier.pressScale(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** Kerangka layar: header (tombol kembali + judul) dan isi yang bisa di-scroll. */
@Composable
fun ScreenColumn(
    title: String,
    onBack: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            content = content
        )
    }
}

/** Muncul sambil naik dari bawah. index makin besar = muncul makin belakangan. */
@Composable
fun Modifier.reveal(index: Int): Modifier {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(550, delayMillis = index * 70, easing = FastOutSlowInEasing),
        label = "reveal"
    )
    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 32.dp.toPx()
    }
}

/** Pembungkus untuk blok yang tidak punya parameter modifier. */
@Composable
fun Reveal(index: Int, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().reveal(index)) { content() }
}