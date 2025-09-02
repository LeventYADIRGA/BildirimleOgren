package com.lyadirga.bildirimleogren.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lyadirga.bildirimleogren.data.remote.fetchSheetCsvFlow
import com.lyadirga.bildirimleogren.model.Language
import com.lyadirga.bildirimleogren.model.LanguageSet
import com.opencsv.CSVReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.StringReader
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {

    private val _languageSets = MutableStateFlow<List<LanguageSet>>(emptyList())
    val languageSets: StateFlow<List<LanguageSet>> get() = _languageSets

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> get() = _errorEvent


    fun fetchSheets(urls: List<String>) {
        viewModelScope.launch {
            try {
                // Her URL için paralel fetch başlat
                val sets = urls.map { url ->
                    async(Dispatchers.IO) { fetchCsvFromUrl(url) }
                }.awaitAll() // URL sırasına göre liste halinde döner

                _languageSets.value = sets

            } catch (e: Exception) {
                _errorEvent.emit(e.localizedMessage ?: "Beklenmeyen bir hata oluştu.")
            }
        }
    }

    // Helper suspend function
    private fun fetchCsvFromUrl(url: String): LanguageSet {
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

        return LanguageSet(title = setTitle, items = entries, url = url)
    }

}
