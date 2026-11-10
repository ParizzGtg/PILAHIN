package com.example.pilahin.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.TextMuted
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Mengatur seluruh alur: izin lokasi -> GPS aktif -> ambil posisi,
 * dan memantau GPS secara realtime selama layar ini tampil.
 * Mengembalikan fungsi "mulai" untuk dipanggil saat layar dibuka atau saat user menekan coba lagi.
 */
@SuppressLint("MissingPermission")
@Composable
fun rememberLocationRequester(
    onLocation: (latitude: Double, longitude: Double) -> Unit,
    onNeedsAction: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val currentOnLocation by rememberUpdatedState(onLocation)
    val currentOnNeedsAction by rememberUpdatedState(onNeedsAction)

    var showPermissionDialog by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    var showGpsDialog by remember { mutableStateOf(false) }
    var waitingSettings by remember { mutableStateOf(false) }
    // true selama dialog "aktifkan lokasi" sedang diproses, agar tidak muncul ganda
    var resolving by remember { mutableStateOf(false) }

    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    fun isGpsOn(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(manager)
    }

    var gpsWasOn by remember { mutableStateOf(isGpsOn()) }

    fun fetchLocation() {
        if (!hasPermission()) {
            currentOnNeedsAction()
            return
        }
        fused.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                currentOnLocation(location.latitude, location.longitude)
            } else {
                fused.lastLocation
                    .addOnSuccessListener { last ->
                        if (last != null) currentOnLocation(last.latitude, last.longitude)
                        else currentOnNeedsAction()
                    }
                    .addOnFailureListener { currentOnNeedsAction() }
            }
        }.addOnFailureListener { currentOnNeedsAction() }
    }

    // Hasil dialog bawaan Google "aktifkan lokasi"
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        resolving = false
        if (result.resultCode == Activity.RESULT_OK) {
            fetchLocation()
        } else {
            showGpsDialog = true
            currentOnNeedsAction()
        }
    }

    fun checkGps() {
        if (resolving) return
        resolving = true

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L).build()
        val settings = LocationSettingsRequest.Builder()
            .addLocationRequest(request)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(context)
            .checkLocationSettings(settings)
            .addOnSuccessListener {
                resolving = false
                fetchLocation()
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        settingsLauncher.launch(IntentSenderRequest.Builder(e.resolution).build())
                    } catch (ex: Exception) {
                        resolving = false
                        showGpsDialog = true
                        currentOnNeedsAction()
                    }
                } else {
                    resolving = false
                    showGpsDialog = true
                    currentOnNeedsAction()
                }
            }
    }

    // Hasil dialog izin lokasi bawaan HP
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            checkGps()
        } else {
            permanentlyDenied = activity?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: false
            showPermissionDialog = true
            currentOnNeedsAction()
        }
    }

    // Saat kembali ke aplikasi (dari Pengaturan atau latar belakang): cek ulang otomatis
    DisposableEffect(activity) {
        val owner = activity as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (waitingSettings && hasPermission()) {
                    waitingSettings = false
                    checkGps()
                } else if (hasPermission() && !isGpsOn()) {
                    gpsWasOn = false
                    currentOnNeedsAction()
                    checkGps()
                }
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    // REALTIME: pantau GPS dinyalakan/dimatikan selama layar ini tampil
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (!hasPermission()) return
                val on = isGpsOn()
                if (on == gpsWasOn) return
                gpsWasOn = on
                if (on) {
                    fetchLocation()
                } else {
                    currentOnNeedsAction()
                    checkGps()
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    fun requestPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            containerColor = Color.White,
            title = { Text("Izin lokasi diperlukan", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "PILAHIN membutuhkan izin lokasi untuk menampilkan bank sampah terdekat " +
                            "dari posisi Anda. Aplikasi tidak dapat berfungsi tanpa izin ini.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    if (permanentlyDenied) {
                        waitingSettings = true
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    } else {
                        requestPermission()
                    }
                }) {
                    Text(
                        if (permanentlyDenied) "Buka Pengaturan" else "Izinkan",
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Nanti", color = TextMuted)
                }
            }
        )
    }

    if (showGpsDialog) {
        AlertDialog(
            onDismissRequest = { showGpsDialog = false },
            containerColor = Color.White,
            title = { Text("Aktifkan GPS", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Nyalakan GPS agar aplikasi dapat menemukan bank sampah di sekitar Anda. " +
                            "Tanpa GPS, fitur peta tidak berfungsi.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showGpsDialog = false
                    checkGps()
                }) {
                    Text("Aktifkan", color = GreenPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDialog = false }) {
                    Text("Nanti", color = TextMuted)
                }
            }
        )
    }

    return {
        if (hasPermission()) checkGps() else requestPermission()
    }
}