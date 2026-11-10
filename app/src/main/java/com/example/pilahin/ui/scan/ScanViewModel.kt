package com.example.pilahin.ui.scan

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pilahin.data.ClassifierRepository
import com.example.pilahin.data.FakeClassifierRepository
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

    // Nanti ganti dengan implementasi Retrofit ke Flask + YOLOv8
    private val classifier: ClassifierRepository = FakeClassifierRepository()
    private val historyRepo = HistoryRepository(app)

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val items = withContext(Dispatchers.IO) { historyRepo.getAll() }
            _state.update { it.copy(history = items) }
        }
    }

    fun classify(file: File, onDone: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(processing = true) }
            runClassify(file, onDone)
        }
    }

    fun classifyUri(uri: Uri, onDone: () -> Unit) {
        viewModelScope.launch {
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
            _state.update {
                it.copy(processing = false, error = "Gagal menganalisis gambar. Coba lagi.")
            }
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
}