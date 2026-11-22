package com.example.pilahin.ui.pickup

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.ApiClient
import com.example.pilahin.data.AppNotification
import com.example.pilahin.data.NotificationRepository
import com.example.pilahin.data.PickupRepository
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.data.PickupStatus
import com.example.pilahin.data.SessionManager
import com.example.pilahin.data.WasteCategory
import com.example.pilahin.data.parsePickups
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class PickupViewModel(private val app: Application) : AndroidViewModel(app) {

    private val pickupRepo = PickupRepository(app)
    private val notifRepo = NotificationRepository(app)
    private val session = SessionManager(app)

    private val _requests = MutableStateFlow(pickupRepo.getAll())
    val requests: StateFlow<List<PickupRequest>> = _requests.asStateFlow()

    private val _notifications = MutableStateFlow(notifRepo.getAll())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        // Ambil status terbaru dari server secara berkala (admin bisa mengubahnya kapan saja)
        viewModelScope.launch {
            while (true) {
                fetch()
                delay(POLL_MS)
            }
        }
    }

    private fun toast(message: String) =
        Toast.makeText(app, message, Toast.LENGTH_LONG).show()

    private suspend fun fetch() {
        val token = session.token ?: return
        val res = ApiClient.request("GET", "/api/pickups", token = token)
        if (!res.isSuccess) return
        val list = parsePickups(res.body.optJSONArray("pickups"))
        notifyChanges(_requests.value, list)
        updateRequests(list)
    }

    /** Buat notifikasi saat admin mengubah status request. */
    private fun notifyChanges(old: List<PickupRequest>, new: List<PickupRequest>) {
        val before = old.associateBy { it.id }
        new.forEach { r ->
            val prev = before[r.id] ?: return@forEach
            if (prev.status == r.status && prev.slot == r.slot) return@forEach
            when (r.status) {
                PickupStatus.DITERIMA -> {
                    val jam = if (r.slot.isNotBlank()) ", pukul ${r.slot}" else ""
                    pushNotification(
                        "Request disetujui",
                        "Petugas akan menjemput ${formatDay(r.dayMillis)}$jam. Siapkan sampahmu."
                    )
                }
                PickupStatus.DITOLAK -> {
                    val alasan = if (r.adminNote.isNotBlank()) " Alasan: ${r.adminNote}" else ""
                    pushNotification(
                        "Request ditolak",
                        "Request jemput ${formatDay(r.dayMillis)} ditolak.$alasan"
                    )
                }
                PickupStatus.SELESAI -> pushNotification(
                    "Penjemputan selesai",
                    "Sampahmu pada ${formatDay(r.dayMillis)} sudah dijemput. Terima kasih!"
                )
                else -> Unit
            }
        }
    }

    /** Parameter slot diabaikan: jam penjemputan ditentukan admin. */
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
        viewModelScope.launch {
            val token = session.token
            if (token == null) {
                toast("Sesi tidak valid, silakan login ulang")
                return@launch
            }
            val body = JSONObject()
                .put("address", address)
                .put("detail", detail)
                .put("latitude", latitude)
                .put("longitude", longitude)
                .put("day", dayMillis)
                .put("types", JSONArray(types.map { it.name }))
                .put("note", note)
            val res = ApiClient.request("POST", "/api/pickups", body, token)
            if (res.isSuccess) {
                pushNotification(
                    "Request terkirim",
                    "Menunggu konfirmasi admin untuk ${formatDay(dayMillis)}."
                )
                fetch()
            } else {
                toast(res.message)
            }
        }
    }

    fun cancel(id: String) {
        viewModelScope.launch {
            val token = session.token ?: return@launch
            val res = ApiClient.request("POST", "/api/pickups/$id/cancel", token = token)
            if (res.isSuccess) {
                pushNotification("Request dibatalkan", "Request jemput kamu telah dibatalkan.")
                fetch()
            } else {
                toast(res.message)
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            val token = session.token ?: return@launch
            val res = ApiClient.request("DELETE", "/api/pickups/$id", token = token)
            if (res.isSuccess) fetch() else toast(res.message)
        }
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
        const val POLL_MS = 10_000L
    }
}