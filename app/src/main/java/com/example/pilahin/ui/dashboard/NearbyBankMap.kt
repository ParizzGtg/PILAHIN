package com.example.pilahin.ui.dashboard

import android.graphics.Color as AndroidColor
import android.graphics.drawable.GradientDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pilahin.data.BankSampah
import com.example.pilahin.data.LatLng
import java.io.File
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun NearbyBankMap(
    user: LatLng,
    banks: List<BankSampah>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().apply {
            userAgentValue = "PILAHIN-StudentApp/1.0 (kontak: septioparis@gmail.com)"
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(16.0)
        }
    }

    // Ikuti lifecycle Activity
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

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()

            val bankIcon = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF2E7D32.toInt())
                setStroke(6, AndroidColor.WHITE)
                setSize(60, 60)
            }
            banks.forEach { bank ->
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
                setSize(52, 52)
            }
            val userPoint = GeoPoint(user.latitude, user.longitude)
            map.overlays.add(
                Marker(map).apply {
                    position = userPoint
                    title = "Lokasi Anda"
                    icon = userIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
            )

            map.controller.setCenter(userPoint)
            map.invalidate()
        }
    )
}