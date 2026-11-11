package com.example.pilahin.ui.dashboard

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pilahin.data.BankSampah
import com.example.pilahin.data.LatLng
import com.example.pilahin.ui.common.CardBorderStroke
import com.example.pilahin.ui.theme.BlobGreen
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.io.File
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private fun distanceMeters(a: LatLng, b: LatLng): Float {
    val result = FloatArray(1)
    Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
    return result[0]
}

private fun formatDistance(meters: Float): String =
    if (meters < 1000f) "${meters.toInt()} m"
    else "%.1f km".format(meters / 1000f).replace('.', ',')

@Composable
fun FullMapScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.userLocation
    val context = LocalContext.current

    if (user == null) {
        NoLocationPlaceholder(onBack)
        return
    }

    val mapView = remember {
        Configuration.getInstance().apply {
            // Sama dengan yang dipakai di NearbyBankMap.kt
            userAgentValue = "PILAHIN-StudentApp/1.0 (kontak: emailkamu@gmail.com)"
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)   // cubit untuk zoom, geser bebas
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            minZoomLevel = 4.0
            maxZoomLevel = 20.0
            controller.setZoom(16.5)
            controller.setCenter(GeoPoint(user.latitude, user.longitude))
        }
    }

    DisposableEffect(mapView) {
        val owner = context.findActivity() as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        owner?.lifecycle?.addObserver(observer)
        mapView.onResume()
        onDispose {
            owner?.lifecycle?.removeObserver(observer)
            mapView.onPause()
            mapView.onDetach()
        }
    }

    val sortedBanks = remember(state.banks, user) {
        state.banks
            .map { it to distanceMeters(user, it.location) }
            .sortedBy { it.second }
    }

    fun flyTo(point: LatLng, zoom: Double) {
        mapView.controller.animateTo(GeoPoint(point.latitude, point.longitude), zoom, 700L)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.clear()

                val bankIcon = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0xFF2E7D32.toInt())
                    setStroke(6, AndroidColor.WHITE)
                    setSize(64, 64)
                }
                state.banks.forEach { bank ->
                    map.overlays.add(
                        Marker(map).apply {
                            position = GeoPoint(bank.location.latitude, bank.location.longitude)
                            title = bank.name
                            icon = bankIcon.constantState?.newDrawable()?.mutate() ?: bankIcon
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                    )
                }

                val userIcon = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0xFF2F80ED.toInt())
                    setStroke(8, AndroidColor.WHITE)
                    setSize(56, 56)
                }
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(user.latitude, user.longitude)
                        title = "Lokasi Anda"
                        icon = userIcon
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                )
                map.invalidate()
            }
        )

        // Tombol kembali
        MapFab(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            description = "Kembali",
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        )

        // Judul
        Text(
            text = "Bank Sampah Terdekat",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 22.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )

        // Zoom dan lokasi saya
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MapFab(Icons.Rounded.Add, "Perbesar", { mapView.controller.zoomIn() })
            MapFab(Icons.Rounded.Remove, "Perkecil", { mapView.controller.zoomOut() })
            MapFab(Icons.Rounded.MyLocation, "Lokasi saya", { flyTo(user, 17.0) })
        }

        // Daftar bank terdekat
        if (sortedBanks.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedBanks, key = { it.first.id }) { (bank, distance) ->
                    BankCard(
                        bank = bank,
                        distance = formatDistance(distance),
                        onClick = { flyTo(bank.location, 18.0) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapFab(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = GreenDark,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BankCard(bank: BankSampah, distance: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = CardBorderStroke,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(BlobGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocationOn, null, tint = GreenPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(bank.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("$distance dari Anda", fontSize = 12.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun NoLocationPlaceholder(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlobGreen)
    ) {
        MapFab(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            description = "Kembali",
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lokasi belum tersedia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Kembali ke Beranda lalu aktifkan lokasi agar peta bisa ditampilkan.",
                fontSize = 13.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}