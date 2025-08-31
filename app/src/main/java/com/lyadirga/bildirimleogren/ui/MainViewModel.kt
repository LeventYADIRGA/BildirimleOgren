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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    private val _wordSets = MutableStateFlow<List<WordSet>>(emptyList())
    val wordSets: StateFlow<List<WordSet>> get() = _wordSets

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> get() = _errorEvent

    fun fetchSheets(urls: List<String>) {
        viewModelScope.launch {
            try {
                val flows = urls.map { fetchSheetCsvFlow(it) } // Her URL için Flow oluştur
                combine(flows) { arrayOfEntries ->
                    arrayOfEntries.toList() // Liste haline getir
                }.catch { e ->
                    _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
                }.collect { sets ->
                    _wordSets.value = sets
                }
            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
            }
        }
    }

}