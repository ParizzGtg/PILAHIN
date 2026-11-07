package com.example.pilahin.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.R
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.PilahinTheme
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextMuted
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.85f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        // MASUK: logo muncul dengan efek pegas, slogan menyusul
        coroutineScope {
            launch { logoAlpha.animateTo(1f, tween(700)) }
            launch {
                logoScale.animateTo(
                    1f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                delay(450)
                launch { textAlpha.animateTo(1f, tween(600)) }
                launch { textOffset.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
            }
        }

        delay(1100)

        // KELUAR: logo membesar sedikit sambil memudar
        coroutineScope {
            launch { logoScale.animateTo(1.15f, tween(450, easing = FastOutSlowInEasing)) }
            launch { logoAlpha.animateTo(0f, tween(450)) }
            launch { textAlpha.animateTo(0f, tween(300)) }
        }

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .systemBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_pilahin),
            contentDescription = "Logo Pilahin",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .graphicsLayer {
                    alpha = textAlpha.value
                    translationY = textOffset.value.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Langkah kecil, dampak besar untuk bumi.",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    PilahinTheme {
        SplashScreen(onSplashFinished = {})
    }
}