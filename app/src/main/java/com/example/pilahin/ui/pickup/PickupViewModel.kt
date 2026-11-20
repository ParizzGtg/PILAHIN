package com.example.pilahin.ui.pickup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.AppNotification
import com.example.pilahin.data.NotificationRepository
import com.example.pilahin.data.PickupRepository
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.data.PickupStatus
import com.example.pilahin.data.WasteCategory
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PickupViewModel(app: Application) : AndroidViewModel(app) {

    // Nanti ganti dengan implementasi backend asli
    private val pickupRepo = PickupRepository(app)
    private val notifRepo = NotificationRepository(app)

    private val _requests = MutableStateFlow(pickupRepo.getAll())
    val requests: StateFlow<List<PickupRequest>> = _requests.asStateFlow()

    private val _notifications = MutableStateFlow(notifRepo.getAll())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        // SIMULASI: request yang menunggu akan "diterima" setelah 15 detik
        val now = System.currentTimeMillis()
        _requests.value
            .filter { it.status == PickupStatus.MENUNGGU }
            .forEach { scheduleAccept(it.id, maxOf(0L, ACCEPT_DELAY - (now - it.createdMillis))) }
    }

    fun create(
        address: String,
        detail: String,
        latitude: Double,
        longitude: Double,
        dayMillis: Long,
        slot: String,
        types: List<WasteCategory>,
        note: String
    ) {
        val request = PickupRequest(
            id = UUID.randomUUID().toString(),
            address = address,
            detail = detail,
            latitude = latitude,
            longitude = longitude,
            dayMillis = dayMillis,
            slot = slot,
            types = types,
            note = note,
            status = PickupStatus.MENUNGGU,
            createdMillis = System.currentTimeMillis()
        )
        updateRequests(_requests.value + request)
        pushNotification(
            "Request terkirim",
            "Menunggu konfirmasi petugas untuk ${formatDay(dayMillis)}."
        )
        scheduleAccept(request.id, ACCEPT_DELAY)
    }

    fun cancel(id: String) {
        val target = _requests.value.firstOrNull { it.id == id } ?: return
        if (target.status != PickupStatus.MENUNGGU && target.status != PickupStatus.DITERIMA) return
        updateRequests(_requests.value.map {
            if (it.id == id) it.copy(status = PickupStatus.DIBATALKAN) else it
        })
        pushNotification("Request dibatalkan", "Request jemput ${formatDay(target.dayMillis)} telah dibatalkan.")
    }

    fun delete(id: String) {
        updateRequests(_requests.value.filter { it.id != id })
    }

    fun markAllRead() {
        if (_notifications.value.none { !it.read }) return
        updateNotifications(_notifications.value.map { it.copy(read = true) })
    }

    fun deleteNotification(id: String) {
        updateNotifications(_notifications.value.filter { it.id != id })
    }

    fun clearNotifications() {
        updateNotifications(emptyList())
    }

    private fun scheduleAccept(id: String, delayMs: Long) {
        viewModelScope.launch {
            delay(delayMs)
            val target = _requests.value.firstOrNull { it.id == id } ?: return@launch
            if (target.status != PickupStatus.MENUNGGU) return@launch
            updateRequests(_requests.value.map {
                if (it.id == id) it.copy(status = PickupStatus.DITERIMA) else it
            })
            pushNotification(
                "Request diterima",
                "Petugas akan menjemput ${formatDay(target.dayMillis)}. Siapkan sampahmu."
            )
        }
    }

    private fun updateRequests(list: List<PickupRequest>) {
        _requests.value = list
        pickupRepo.saveAll(list)
    }

    private fun updateNotifications(list: List<AppNotification>) {
        _notifications.value = list
        notifRepo.saveAll(list)
    }

    private fun pushNotification(title: String, message: String) {
        val item = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            timeMillis = System.currentTimeMillis(),
            read = false
        )
        updateNotifications(listOf(item) + _notifications.value)
    }

    private companion object {
        const val ACCEPT_DELAY = 15_000L
    }
}