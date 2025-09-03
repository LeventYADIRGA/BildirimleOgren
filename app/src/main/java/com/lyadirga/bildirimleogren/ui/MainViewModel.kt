package com.lyadirga.bildirimleogren.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyadirga.bildirimleogren.data.LanguageSetWithItems
import com.lyadirga.bildirimleogren.data.Repository
import com.lyadirga.bildirimleogren.model.Language
import com.lyadirga.bildirimleogren.model.LanguageSet
import com.opencsv.CSVReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.StringReader
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    val setAllSetSummariesFlow = repository.getAllSetSummariesFlow() // Flow<List<LanguageSetSummary>>

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> get() = _errorEvent

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading


    fun fetchSheetsFromDbUrls() {
        viewModelScope.launch {
            try {
                // DB’deki mevcut setlerin URL’lerini al
                val existingSets = repository.getAllSetsFlow().first() // Flow’u bir kere al
                val urls = existingSets.mapNotNull { it.set.url }

                if (urls.isEmpty()) return@launch // URL yoksa çık


                // Her URL için paralel fetch başlat
                val sets = urls.map { url ->
                    async(Dispatchers.IO) { fetchCsvFromUrl(url) }
                }.awaitAll() // URL sırasına göre liste halinde döner

                // DB’yi güncelle
                repository.insertOrUpdateSets(sets)

            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
            }
        }
    }

    fun fetchSingleSheet(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newSet = fetchCsvFromUrl(url)
                repository.insertOrUpdateSets(listOf(newSet))
            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSetById(setId)
            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Set silinirken bir hata oluştu.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSetDetails(setId: Long, onResult: (LanguageSetWithItems?) -> Unit) {
        viewModelScope.launch {
            try {
                val set = repository.getSetById(setId)
                onResult(set)
            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Set detayları alınırken bir hata oluştu.")
                onResult(null)
            }
        }
    }

    // Helper suspend function
    private suspend fun fetchCsvFromUrl(url: String): LanguageSet = withContext(Dispatchers.IO) {
        val csvText = URL(url).readText() // IO thread'te çalışır

        val reader = CSVReader(StringReader(csvText))
        val rows = reader.readAll()

        val setTitle = rows.firstOrNull()?.firstOrNull() ?: ""
        val dataRows = rows.drop(1)

        val entries = dataRows.map { row ->
            Language(
                wordOrSentence = row.getOrElse(0) { "" },
                meaning = row.getOrElse(1) { "" }
            )
        }

        LanguageSet(title = setTitle, items = entries, url = url)
    }

}
