package com.lyadirga.bildirimleogren.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyadirga.bildirimleogren.data.remote.WordSet
import com.lyadirga.bildirimleogren.data.remote.fetchSheetCsvFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _wordSet = MutableStateFlow<WordSet?>(null)
    val wordSet: StateFlow<WordSet?> get() = _wordSet

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> get() = _errorEvent

    fun fetchSheet(url: String) {
        viewModelScope.launch {
            try {
                fetchSheetCsvFlow(url)
                    .catch { e ->
                        _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
                        _wordSet.value = WordSet("", emptyList())
                    }
                    .collect { wordSet ->
                        _wordSet.value = wordSet
                    }
            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
            }
        }
    }

}