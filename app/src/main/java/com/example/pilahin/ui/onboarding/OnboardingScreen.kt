package com.example.pilahin.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.R
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.IndicatorInactive
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.PilahinTheme
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

/* ---------------------------------------------------------------
 * DATA HALAMAN
 * --------------------------------------------------------------- */

private data class OnboardingPageData(
    val label: String,
    val title: String,
    val description: String,
    val imageRes: Int
)

private val onboardingPages = listOf(
    OnboardingPageData(
        label = "KENALI DENGAN AI",
        title = "Kenali jenis sampah dengan cerdas",
        description = "Arahkan kamera ke sampah. AI membantu mengenali jenis sampahnya dan cara memilahnya.",
        imageRes = R.drawable.onboarding_1
    ),
    OnboardingPageData(
        label = "PILAH DARI RUMAH",
        title = "Pilah dengan tepat",
        description = "Organik, anorganik, atau B3? Temukan panduan praktis agar setiap sampah masuk ke tempat yang tepat.",
        imageRes = R.drawable.onboarding_2
    ),
    OnboardingPageData(
        label = "KEBIASAAN BAIK, BUMI LEBIH BAIK",
        title = "Kelola untuk lingkungan yang lebih baik",
        description = "Jangan lewatkan jadwal pengangkutan. Aktifkan pengingat dan mulai kebiasaan baik untuk bumi.",
        imageRes = R.drawable.onboarding_3
    )
)

/* ---------------------------------------------------------------
 * HELPER ANIMASI MASUK BERTAHAP
 * --------------------------------------------------------------- */

@Composable
private fun Modifier.entrance(started: Boolean, delayMillis: Int): Modifier {
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(600, delayMillis, FastOutSlowInEasing),
        label = "entrance"
    )
    return this.graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 40.dp.toPx()
    }
}

/* ---------------------------------------------------------------
 * LAYAR UTAMA
 * --------------------------------------------------------------- */

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == onboardingPages.lastIndex

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    // Tombol back: kembali ke halaman sebelumnya
    BackHandler(enabled = currentPage > 0) {
        scope.launch {
            pagerState.animateScrollToPage(
                currentPage - 1,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .systemBarsPadding()
    ) {
        Box(modifier = Modifier.entrance(started, 0)) {
            OnboardingTopBar(
                showSkip = !isLastPage,
                onSkip = onFinished
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .entrance(started, 150)
        ) { page ->
            OnboardingPageContent(
                data = onboardingPages[page],
                pageOffset = {
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                }
            )
        }

        PagerIndicator(
            count = onboardingPages.size,
            current = currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 20.dp)
                .entrance(started, 300)
        )

        Button(
            onClick = {
                if (isLastPage) {
                    onFinished()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            currentPage + 1,
                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Lime,
                contentColor = TextDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
                .height(58.dp)
                .entrance(started, 450)
        ) {
            AnimatedContent(
                targetState = isLastPage,
                transitionSpec = {
                    (slideInVertically(tween(300)) { it / 2 } + fadeIn(tween(300))) togetherWith
                            (slideOutVertically(tween(200)) { -it / 2 } + fadeOut(tween(150)))
                },
                label = "buttonText"
            ) { last ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (last) "Mulai Registrasi" else "Lanjut",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------------
 * KOMPONEN
 * --------------------------------------------------------------- */

@Composable
private fun OnboardingTopBar(
    showSkip: Boolean,
    onSkip: () -> Unit
) {
    val skipAlpha by animateFloatAsState(
        targetValue = if (showSkip) 1f else 0f,
        animationSpec = tween(300),
        label = "skipAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo Pilahin (transparan) menggantikan ikon daun + tulisan
        Image(
            painter = painterResource(id = R.drawable.logo_pilahin_bersih),
            contentDescription = "Logo Pilahin",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(72.dp)
        )

        Text(
            text = "Lewati",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            modifier = Modifier
                .alpha(skipAlpha)
                .clip(RoundedCornerShape(10.dp))
                .clickable(enabled = showSkip, onClick = onSkip)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun OnboardingPageContent(
    data: OnboardingPageData,
    pageOffset: () -> Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Gambar (parallax lambat, fade, dan mengecil) + melayang pelan
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    val offset = pageOffset()
                    val dist = offset.absoluteValue.coerceAtMost(1f)
                    translationX = offset * size.width * 0.4f
                    alpha = (1f - dist * 1.2f).coerceIn(0f, 1f)
                    val s = 1f - 0.15f * dist
                    scaleX = s
                    scaleY = s
                },
            contentAlignment = Alignment.Center
        ) {
            val transition = rememberInfiniteTransition(label = "float")
            val dy by transition.animateFloat(
                initialValue = -1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dy"
            )
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = dy * 6.dp.toPx() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Teks (parallax lebih cepat)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
                .graphicsLayer {
                    val offset = pageOffset()
                    val dist = offset.absoluteValue.coerceAtMost(1f)
                    translationX = offset * size.width * -0.3f
                    alpha = (1f - dist * 1.5f).coerceIn(0f, 1f)
                }
        ) {
            Text(
                text = data.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = data.title,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = data.description,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun PagerIndicator(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = index == current
            val width by animateDpAsState(
                targetValue = if (selected) 28.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "dotWidth"
            )
            val color by animateColorAsState(
                targetValue = if (selected) GreenPrimary else IndicatorInactive,
                animationSpec = tween(300),
                label = "dotColor"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    PilahinTheme {
        OnboardingScreen(onFinished = {})
    }
}