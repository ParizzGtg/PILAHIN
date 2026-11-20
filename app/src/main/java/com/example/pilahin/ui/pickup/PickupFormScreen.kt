package com.example.pilahin.ui.pickup

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pilahin.data.LatLng
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.ui.auth.PrimaryButton
import com.example.pilahin.ui.common.CardBorderStroke
import com.example.pilahin.ui.common.ScreenColumn
import com.example.pilahin.ui.common.reveal
import com.example.pilahin.ui.dashboard.NearbyBankMap
import com.example.pilahin.ui.dashboard.rememberLocationRequester
import com.example.pilahin.ui.theme.BlobGreen
import com.example.pilahin.ui.theme.ChipBorder
import com.example.pilahin.ui.theme.GreenDark
import com.example.pilahin.ui.theme.GreenPrimary
import com.example.pilahin.ui.theme.Lime
import com.example.pilahin.ui.theme.TextDark
import com.example.pilahin.ui.theme.TextMuted
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Tengah malam hari ini dalam format UTC (format yang dipakai DatePicker). */
private fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** Ubah tanggal pilihan DatePicker (UTC) menjadi tengah malam waktu lokal. */
private fun utcToLocalMidnight(utc: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utc }
    return Calendar.getInstance().apply {
        clear()
        set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
    }.timeInMillis
}

@Suppress("DEPRECATION")
private fun reverseGeocode(context: Context, lat: Double, lon: Double): String {
    val fallback = "%.5f, %.5f".format(Locale.US, lat, lon)
    return try {
        if (!Geocoder.isPresent()) return fallback
        Geocoder(context, Locale("id", "ID"))
            .getFromLocation(lat, lon, 1)
            ?.firstOrNull()
            ?.getAddressLine(0)
            ?: fallback
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickupFormScreen(
    viewModel: PickupViewModel,
    onBack: () -> Unit,
    onSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var location by remember { mutableStateOf<LatLng?>(null) }
    var address by remember { mutableStateOf("") }
    var addressEdited by remember { mutableStateOf(false) }
    var loadingLocation by remember { mutableStateOf(true) }
    var detail by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var types by remember { mutableStateOf(setOf<WasteCategory>()) }
    var showErrors by remember { mutableStateOf(false) }

    val todayUtc = remember { todayUtcMillis() }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = todayUtc,
        selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtc
                override fun isSelectableYear(year: Int) =
                    year >= Calendar.getInstance().get(Calendar.YEAR)
            }
        }
    )
    val selectedLocal = datePickerState.selectedDateMillis?.let { utcToLocalMidnight(it) }

    // GPS: izin, aktifkan lokasi, ambil posisi, lalu ubah jadi alamat
    val requestLocation = rememberLocationRequester(
        onLocation = { lat, lon ->
            location = LatLng(lat, lon)
            scope.launch {
                val text = withContext(Dispatchers.IO) { reverseGeocode(context, lat, lon) }
                if (!addressEdited) address = text
                loadingLocation = false
            }
        },
        onNeedsAction = { loadingLocation = false }
    )
    LaunchedEffect(Unit) { requestLocation() }

    fun refreshFromGps() {
        addressEdited = false
        loadingLocation = true
        requestLocation()
    }

    fun submit() {
        showErrors = true
        val loc = location
        val day = selectedLocal
        if (loc == null || address.isBlank() || day == null || types.isEmpty()) return

        viewModel.create(
            address = address.trim(),
            detail = detail.trim(),
            latitude = loc.latitude,
            longitude = loc.longitude,
            dayMillis = day,
            slot = "",
            types = types.toList(),
            note = note.trim()
        )
        Toast.makeText(context, "Request jemput terkirim", Toast.LENGTH_SHORT).show()
        onSubmitted()
    }

    ScreenColumn(title = "Request Jemput", onBack = onBack) {
        // Peta titik jemput
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .reveal(0)
                .clip(RoundedCornerShape(20.dp))
                .background(BlobGreen)
        ) {
            val loc = location
            if (loc != null) {
                NearbyBankMap(user = loc, banks = emptyList(), modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {}
                )
                Text(
                    text = "Titik jemput",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (loadingLocation) {
                        CircularProgressIndicator(
                            color = GreenPrimary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("Mencari lokasi Anda…", fontSize = 13.sp, color = TextMuted)
                    } else {
                        Icon(Icons.Rounded.LocationOff, null, tint = GreenDark, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("Lokasi belum aktif", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = { refreshFromGps() },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Lime, contentColor = TextDark)
                        ) { Text("Gunakan lokasi saya", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    }
                }
            }
        }
        if (showErrors && location == null) {
            Text(
                text = "Aktifkan lokasi agar titik jemput akurat.",
                fontSize = 12.sp,
                color = Color(0xFFC62828),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        // Alamat (otomatis dari GPS, bisa diedit)
        Column(modifier = Modifier.reveal(1)) {
            SectionLabel("Alamat jemput")
            FormField(
                value = address,
                onValueChange = { address = it; addressEdited = true },
                label = if (loadingLocation && address.isBlank()) "Mengambil alamat…" else "Alamat",
                minLines = 2,
                error = if (showErrors && address.isBlank()) "Alamat belum terisi" else null
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { refreshFromGps() }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.MyLocation, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                Text(
                    text = "  Perbarui dari GPS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary
                )
            }
            Spacer(Modifier.height(4.dp))
            FormField(
                value = detail,
                onValueChange = { detail = it },
                label = "Detail alamat (patokan, no. rumah)"
            )
        }

        Spacer(Modifier.height(14.dp))

        // Tanggal: kalender langsung
        Column(modifier = Modifier.reveal(2)) {
            SectionLabel("Pilih tanggal penjemputan")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = CardBorderStroke,
                shadowElevation = 2.dp
            ) {
                DatePicker(
                    state = datePickerState,
                    title = null,
                    headline = null,
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        selectedDayContainerColor = GreenPrimary,
                        selectedDayContentColor = Color.White,
                        todayContentColor = GreenPrimary,
                        todayDateBorderColor = GreenPrimary,
                        selectedYearContainerColor = GreenPrimary,
                        selectedYearContentColor = Color.White
                    )
                )
            }
            selectedLocal?.let {
                Text(
                    text = "Dipilih: ${formatDay(it)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (showErrors && selectedLocal == null) {
                Text(
                    text = "Pilih tanggal penjemputan.",
                    fontSize = 12.sp,
                    color = Color(0xFFC62828),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Jenis sampah
        Column(modifier = Modifier.reveal(3)) {
            SectionLabel("Jenis sampah yang dijemput")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WasteCategory.entries.forEach { category ->
                    SelectChip(
                        label = category.label,
                        selected = category in types
                    ) {
                        types = if (category in types) types - category else types + category
                    }
                }
            }
            if (showErrors && types.isEmpty()) {
                Text(
                    text = "Pilih minimal satu jenis sampah.",
                    fontSize = 12.sp,
                    color = Color(0xFFC62828),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(modifier = Modifier.reveal(4)) {
            SectionLabel("Catatan untuk petugas (opsional)")
            FormField(
                value = note,
                onValueChange = { note = it },
                label = "Contoh: sampah di depan pagar",
                minLines = 2
            )
        }

        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.reveal(5)) {
            PrimaryButton(text = "Kirim Request", onClick = { submit() })
        }
        Spacer(Modifier.imePadding().height(24.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) GreenDark else TextDark,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Lime else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    )
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    minLines: Int = 1,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        singleLine = minLines == 1,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = ChipBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedLabelColor = GreenPrimary,
            cursorColor = GreenPrimary,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark
        ),
        modifier = Modifier.fillMaxWidth()
    )
}