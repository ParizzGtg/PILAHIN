package com.example.pilahin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.ApiBankSampahRepository
import com.example.pilahin.data.BankSampah
import com.example.pilahin.data.BankSampahRepository
import com.example.pilahin.data.LatLng
import com.example.pilahin.data.distanceKmTo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LocationStatus { CHECKING, READY, NEEDS_ACTION }

data class PickupSchedule(
    val dayLabel: String,
    val date: String,
    val time: String,
    val detail: String,
    val reminderActive: Boolean
)

data class DashboardUiState(
    val userName: String = "Paris",
    val locationStatus: LocationStatus = LocationStatus.CHECKING,
    val userLocation: LatLng? = null,
    val banks: List<BankSampah> = emptyList(),
    val banksLoading: Boolean = false,
    // Pesan bila bank sampah kosong / gagal dimuat (null = tidak ada masalah)
    val bankMessage: String? = null,
    // Data contoh, nanti dari backend
    val pickup: PickupSchedule = PickupSchedule(
        dayLabel = "SAB",
        date = "31",
        time = "Besok, 07.00–09.00",
        detail = "Sampah organik • Melati, RT 03",
        reminderActive = true
    )
)

class DashboardViewModel : ViewModel() {

    // Data asli dari server: bank sampah di sekitar lokasi GPS pengguna
    private val repository: BankSampahRepository = ApiBankSampahRepository()
    private var lastSearch: LatLng? = null
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun onLocationChecking() {
        _uiState.update { it.copy(locationStatus = LocationStatus.CHECKING) }
    }

    fun onLocationReady(latitude: Double, longitude: Double) {
        val user = LatLng(latitude, longitude)
        // Peta langsung tampil di lokasi pengguna, daftar bank sampah menyusul
        _uiState.update { it.copy(locationStatus = LocationStatus.READY, userLocation = user) }

        // Tidak perlu cari ulang kalau pengguna baru bergeser kurang dari 300 m
        val last = lastSearch
        val current = _uiState.value
        if (last != null && last.distanceKmTo(user) < 0.3 &&
            current.bankMessage == null && !current.banksLoading
        ) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(banksLoading = true, bankMessage = null) }
            try {
                val banks = repository.getNearby(user)
                lastSearch = user
                _uiState.update {
                    it.copy(
                        banks = banks,
                        banksLoading = false,
                        bankMessage = if (banks.isEmpty()) {
                            "Belum ada bank sampah yang terdata dalam radius " +
                                    "${ApiBankSampahRepository.RADIUS_KM} km dari lokasi Anda."
                        } else null
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        banksLoading = false,
                        bankMessage = e.message ?: "Gagal memuat bank sampah. Coba lagi."
                    )
                }
            }
        }
    }

    /** Dipanggil dari tombol "Coba lagi" di peta. */
    fun retryBanks() {
        val user = _uiState.value.userLocation ?: return
        lastSearch = null
        _uiState.update { it.copy(bankMessage = null) }
        onLocationReady(user.latitude, user.longitude)
    }

    fun onLocationNeedsAction() {
        _uiState.update { it.copy(locationStatus = LocationStatus.NEEDS_ACTION) }
    }
}