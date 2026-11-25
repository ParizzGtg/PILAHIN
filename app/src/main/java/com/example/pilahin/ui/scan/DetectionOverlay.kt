package com.example.pilahin.ui.scan

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.DetectionFrame
import com.example.pilahin.ui.common.color
import kotlin.math.max

/** Menggambar kotak + label di atas preview kamera (mode FILL_CENTER). */
@Composable
fun DetectionOverlay(frame: DetectionFrame?, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val colors = frame?.items?.map { it.category.color() } ?: emptyList()

    Canvas(modifier) {
        if (frame == null || frame.width <= 0 || frame.height <= 0) return@Canvas
        val scale = max(size.width / frame.width, size.height / frame.height)
        val dx = (frame.width * scale - size.width) / 2f
        val dy = (frame.height * scale - size.height) / 2f

        frame.items.forEachIndexed { i, d ->
            val color = colors.getOrElse(i) { Color.White }
            val l = d.left * frame.width * scale - dx
            val t = d.top * frame.height * scale - dy
            val r = d.right * frame.width * scale - dx
            val b = d.bottom * frame.height * scale - dy
            if (r <= l || b <= t) return@forEachIndexed

            drawRoundRect(
                color = color,
                topLeft = Offset(l, t),
                size = Size(r - l, b - t),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            val text = "${d.label} ${(d.confidence * 100).toInt()}% • ${d.category.label}"
            val layout = measurer.measure(
                text,
                TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
            val padX = 6.dp.toPx()
            val padY = 3.dp.toPx()
            val boxW = layout.size.width + padX * 2
            val boxH = layout.size.height + padY * 2
            val top = (t - boxH).coerceAtLeast(0f)
            val left = l.coerceIn(0f, (size.width - boxW).coerceAtLeast(0f))
            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(boxW, boxH),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawText(layout, topLeft = Offset(left + padX, top + padY))
        }
    }
}