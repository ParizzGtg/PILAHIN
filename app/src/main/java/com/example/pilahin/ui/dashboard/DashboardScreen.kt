package com.example.pilahin.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.ui.auth.authEntrance
import com.example.pilahin.ui.common.pressScale
import com.example.pilahin.ui.theme.BlobGreen
import com.example.pilahin.ui.theme.ChipBg
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.SplashBackground
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val CardBorder = BorderStroke(1.dp, Color(0xFFE3EDD9))
private val IdLocale = Locale("id", "ID")

@Composable
fun DashboardScreen(
    userName: String,
    hasUnread: Boolean,
    nextPickup: PickupRequest?,
    onNotifications: () -> Unit,
    onScan: () -> Unit,
    onSchedule: () -> Unit,
    onGuide: () -> Unit,
    onHistory: () -> Unit,
    onOil: () -> Unit,
    onOpenMap: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val requestLocation = rememberLocationRequester(
        onLocation = { lat, lon -> viewModel.onLocationReady(lat, lon) },
        onNeedsAction = { viewModel.onLocationNeedsAction() }
    )

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        started = true
        requestLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        DashboardHeader(
            name = userName,
            hasUnread = hasUnread,
            onBell = onNotifications,
            modifier = Modifier.authEntrance(started, 0)
        )

        Spacer(Modifier.height(16.dp))

        ScanCard(
            onClick = onScan,
            modifier = Modifier.authEntrance(started, 80)
        )

        Spacer(Modifier.height(12.dp))

        PickupCard(
            request = nextPickup,
            onClick = onSchedule,
            modifier = Modifier.authEntrance(started, 160)
        )

        Spacer(Modifier.height(12.dp))

        MapCard(
            state = state,
            onRetry = {
                viewModel.onLocationChecking()
                requestLocation()
            },
            onOpenMap = onOpenMap,
            modifier = Modifier.authEntrance(started, 240)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .authEntrance(started, 320),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mulai kebiasaan baik",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Icon(
                imageVector = Icons.Rounded.Spa,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .authEntrance(started, 380),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HabitCard(
                icon = Icons.Rounded.Layers,
                title = "Panduan Pemilahan",
                subtitle = "Kenali setiap jenisnya",
                onClick = onGuide,
                modifier = Modifier.weight(1f)
            )
            HabitCard(
                icon = Icons.Rounded.LocationOn,
                title = "Peta Bank Sampah",
                subtitle = "Temukan lokasi terdekat",
                onClick = onOpenMap,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .authEntrance(started, 440),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HabitCard(
                icon = Icons.Rounded.History,
                title = "Riwayat",
                subtitle = "Lihat hasil scan kamu",
                onClick = onHistory,
                modifier = Modifier.weight(1f)
            )
            HabitCard(
                icon = Icons.Rounded.WaterDrop,
                title = "Edukasi Minyak Jelantah",
                subtitle = "Ubah sisa jadi manfaat",
                onClick = onOil,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

/* ---------------------------------------------------------------
 * HELPER
 * --------------------------------------------------------------- */

private fun greetingByTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 4..10 -> "Selamat pagi,"
        hour in 11..14 -> "Selamat siang,"
        hour in 15..17 -> "Selamat sore,"
        else -> "Selamat malam,"
    }
}

private fun midnightOf(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun pickupDayLabel(millis: Long): String {
    val diffDays = Math.round(
        (midnightOf(millis) - midnightOf(System.currentTimeMillis())) / 86_400_000.0
    ).toInt()
    return when (diffDays) {
        0 -> "Hari ini"
        1 -> "Besok"
        else -> SimpleDateFormat("EEEE, d MMM yyyy", IdLocale).format(Date(millis))
    }
}

/* ---------------------------------------------------------------
 * KOMPONEN
 * --------------------------------------------------------------- */

@Composable
private fun DashboardHeader(
    name: String,
    hasUnread: Boolean,
    onBell: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = greetingByTime(), fontSize = 14.sp, color = TextMuted)
            Text(
                text = "Halo, $name!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }

        Box(
            modifier = Modifier
                .size(46.dp)
                .pressScale(onBell)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifikasi",
                tint = GreenDark,
                modifier = Modifier.size(24.dp)
            )
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 11.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary)
                )
            }
        }
    }
}

@Composable
private fun ScanCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(onClick)
            .clip(RoundedCornerShape(24.dp))
            .background(Lime)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = GreenDark,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "PEMILAHAN PINTAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GreenDark
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Scan Sampah",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Kenali jenisnya, pilah dengan tepat.",
                    fontSize = 13.sp,
                    color = GreenDark
                )
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DocumentScanner,
                    contentDescription = null,
                    tint = GreenDark,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

@Composable
private fun PickupCard(
    request: PickupRequest?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = CardBorder,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Penjemputan terdekat",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                if (request != null) {
                    Text(
                        text = request.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GreenDark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ChipBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (request == null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ChipBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.LocalShipping, null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Belum ada request jemput",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Ketuk untuk membuat request.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = TextDark)
                }
            } else {
                val dayAbbrev = SimpleDateFormat("EEE", IdLocale)
                    .format(Date(request.dayMillis)).uppercase(IdLocale)
                val dayNumber = SimpleDateFormat("d", IdLocale)
                    .format(Date(request.dayMillis))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Lime),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(dayAbbrev, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GreenDark)
                        Text(dayNumber, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pickupDayLabel(request.dayMillis),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Rounded.LocationOn, null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = request.address,
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(Icons.Rounded.ChevronRight, null, tint = TextDark)
                }
            }
        }
    }
}

@Composable
private fun MapCard(
    state: DashboardUiState,
    onRetry: () -> Unit,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = CardBorder,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Crossfade(
                targetState = state.locationStatus,
                label = "mapState",
                modifier = Modifier.fillMaxSize()
            ) { status ->
                when (status) {
                    LocationStatus.READY -> {
                        val user = state.userLocation
                        if (user != null) {
                            NearbyBankMap(
                                user = user,
                                banks = state.banks,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    LocationStatus.CHECKING -> MapPlaceholder {
                        CircularProgressIndicator(
                            color = GreenPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Mencari lokasi Anda…",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }

                    LocationStatus.NEEDS_ACTION -> MapPlaceholder {
                        Icon(
                            imageVector = Icons.Rounded.LocationOff,
                            contentDescription = null,
                            tint = GreenDark,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Lokasi belum aktif",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Aktifkan lokasi untuk melihat bank sampah terdekat.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = onRetry,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Lime,
                                contentColor = TextDark
                            ),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Aktifkan lokasi", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Peta di dashboard hanya pratinjau; ketuk untuk membuka peta layar penuh.
            if (state.locationStatus == LocationStatus.READY) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onOpenMap)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Map,
                    contentDescription = null,
                    tint = GreenDark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Bank Sampah Terdekat",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            if (state.locationStatus == LocationStatus.READY) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(start = 10.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lihat peta lengkap",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobGreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        content()
    }
}

@Composable
private fun HabitCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(112.dp)
            .pressScale(onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = CardBorder,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Rounded.NorthEast,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}