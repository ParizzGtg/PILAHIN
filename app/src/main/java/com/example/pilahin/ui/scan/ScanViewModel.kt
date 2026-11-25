package com.example.pilahin.ui.scan

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.ApiClassifierRepository
import com.example.pilahin.data.ApiException
import com.example.pilahin.data.ClassifierRepository
import com.example.pilahin.data.DetectionFrame
import com.example.pilahin.data.DetectionRepository
import com.example.pilahin.data.HistoryRepository
import com.example.pilahin.data.ScanResult
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanUiState(
    val processing: Boolean = false,
    val current: ScanResult? = null,
    val saved: Boolean = false,
    val error: String? = null,
    val history: List<ScanResult> = emptyList()
)

class ScanViewModel(private val app: Application) : AndroidViewModel(app) {

    // Foto dikirim ke server, model AI PILAHIN yang menebak kategorinya
    private val classifier: ClassifierRepository = ApiClassifierRepository(app)
    private val detector = DetectionRepository(app)
    private val historyRepo = HistoryRepository(app)

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    /** Hasil deteksi langsung dari kamera (kotak + label). */
    private val _frame = MutableStateFlow<DetectionFrame?>(null)
    val frame: StateFlow<DetectionFrame?> = _frame.asStateFlow()

    @Volatile private var detecting = false
    @Volatile private var failures = 0
    @Volatile private var lastSent = 0L

    init {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { historyRepo.getAll() }
            _state.update { it.copy(history = items) }
        }
    }

    /* ---------- Deteksi langsung ---------- */

    /** Dipanggil saat layar scan dibuka: coba lagi walau sebelumnya gagal. */
    fun resetDetection() {
        failures = 0
        _frame.value = null
    }

    /** Murah dicek dari thread kamera supaya frame tidak diproses sia-sia. */
    fun wantsFrame(): Boolean =
        !detecting && failures < MAX_FAILURES && !_state.value.processing &&
                System.currentTimeMillis() - lastSent >= FRAME_INTERVAL_MS

    fun onFrame(jpeg: ByteArray, width: Int, height: Int) {
        if (!wantsFrame()) return
        detecting = true
        lastSent = System.currentTimeMillis()
        viewModelScope.launch {
            val result = detector.detect(jpeg, width, height)
            result.onSuccess {
                failures = 0
                _frame.value = DetectionFrame(it, width, height)
            }.onFailure {
                failures++
                _frame.value = null
            }
            detecting = false
        }
    }

    /* ---------- Klasifikasi saat foto diambil ---------- */

    fun classify(file: File, onDone: () -> Unit) {
        viewModelScope.launch {
            _frame.value = null
            _state.update { it.copy(processing = true) }
            runClassify(file, onDone)
        }
    }

    fun classifyUri(uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
            _frame.value = null
            _state.update { it.copy(processing = true) }
            try {
                val file = withContext(Dispatchers.IO) {
                    val f = File(app.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
                    app.contentResolver.openInputStream(uri)?.use { input ->
                        f.outputStream().use { input.copyTo(it) }
                    } ?: throw IllegalStateException("Gambar tidak dapat dibuka")
                    f
                }
                runClassify(file, onDone)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(processing = false, error = "Gagal membuka gambar.") }
            }
        }
    }

    private suspend fun runClassify(file: File, onDone: () -> Unit) {
        try {
            val result = classifier.classify(file)
            _state.update { it.copy(processing = false, current = result, saved = false) }
            onDone()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            // Pesan dari server ditampilkan apa adanya (mis. "Tidak ada sampah terdeteksi...")
            val message = (e as? ApiException)?.message ?: "Gagal menganalisis gambar. Coba lagi."
            _state.update { it.copy(processing = false, error = message) }
        }
    }

    fun saveCurrent() {
        val cur = _state.value.current ?: return
        if (_state.value.saved) return
        viewModelScope.launch {
            val savedItem = withContext(Dispatchers.IO) {
                val dir = File(app.filesDir, "history").apply { mkdirs() }
                val dest = File(dir, "${cur.id}.jpg")
                runCatching { File(cur.imagePath).copyTo(dest, overwrite = true) }
                val item = cur.copy(imagePath = dest.absolutePath)
                historyRepo.add(item)
                item
            }
            val items = withContext(Dispatchers.IO) { historyRepo.getAll() }
            _state.update { it.copy(current = savedItem, saved = true, history = items) }
        }
    }

    fun openFromHistory(item: ScanResult) {
        _state.update { it.copy(current = item, saved = true) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    private companion object {
        const val FRAME_INTERVAL_MS = 900L
        const val MAX_FAILURES = 3
    }
}