package com.example.pilahin.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.BankSampah
import com.example.pilahin.data.BankSampahRepository
import com.example.pilahin.data.FakeBankSampahRepository
import com.example.pilahin.data.LatLng
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

    // Nanti ganti dengan implementasi backend asli
    private val repository: BankSampahRepository = FakeBankSampahRepository()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun onLocationChecking() {
        _uiState.update { it.copy(locationStatus = LocationStatus.CHECKING) }
    }

    fun onLocationReady(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val user = LatLng(latitude, longitude)
            val banks = repository.getNearby(user)
            _uiState.update {
                it.copy(
                    locationStatus = LocationStatus.READY,
                    userLocation = user,
                    banks = banks
                )
            }
        }
    }

    fun onLocationNeedsAction() {
        _uiState.update { it.copy(locationStatus = LocationStatus.NEEDS_ACTION) }
    }
}