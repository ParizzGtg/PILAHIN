package com.example.pilahin.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.ApiClient
import com.example.pilahin.data.PickupRequest
import com.example.pilahin.data.PickupStatus
import com.example.pilahin.data.SessionManager
import com.example.pilahin.data.parsePickups
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AdminViewModel(app: Application) : AndroidViewModel(app) {

    private val session = SessionManager(app)

    private val _requests = MutableStateFlow<List<PickupRequest>>(emptyList())
    val requests: StateFlow<List<PickupRequest>> = _requests.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                load()
                delay(POLL_MS)
            }
        }
    }

    private suspend fun load() {
        val token = session.token ?: return
        val res = ApiClient.request("GET", "/api/admin/pickups", token = token)
        if (res.isSuccess) {
            _requests.value = parsePickups(res.body.optJSONArray("pickups"))
            _error.value = null
        } else {
            _error.value = res.message
        }
    }

    /** onResult(null) = berhasil, onResult("pesan") = gagal */
    fun update(
        id: String,
        status: PickupStatus,
        time: String,
        note: String,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val token = session.token
            if (token == null) {
                onResult("Sesi tidak valid, silakan login ulang")
                return@launch
            }
            val body = JSONObject()
                .put("status", status.name)
                .put("pickup_time", time)
                .put("admin_note", note)
            val res = ApiClient.request("PUT", "/api/admin/pickups/$id", body, token)
            if (res.isSuccess) {
                load()
                onResult(null)
            } else {
                onResult(res.message)
            }
        }
    }

    private companion object {
        const val POLL_MS = 10_000L
    }
}