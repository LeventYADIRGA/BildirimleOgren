package com.lyadirga.bildirimleogren.data.remote

import com.lyadirga.bildirimleogren.model.Language
import com.lyadirga.bildirimleogren.model.LanguageSet
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.StringReader
import java.net.URL

val SHEET_URL1 = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTO8rQRuzuW4R1t94Bpfd-Dsc8sR-ZZdlGiAvoFmP9_61bgaaHsOU2GynonalYSc-bdNHNjwqIMG20p/pub?gid=0&single=true&output=csv"
val SHEET_URL2 = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTO8rQRuzuW4R1t94Bpfd-Dsc8sR-ZZdlGiAvoFmP9_61bgaaHsOU2GynonalYSc-bdNHNjwqIMG20p/pub?gid=1239646743&single=true&output=csv"


fun fetchSheetCsvFlow(url: String): Flow<LanguageSet> = flow {
    try {
        // CSV verisini çek
        val csvText = URL(url).readText()

        // OpenCSV kullanarak parse et
        val reader = CSVReader(StringReader(csvText))
        val rows = reader.readAll()

        // İlk satır başlık (örneğin Çalışma Seti Başlığı)
        val setTitle = rows.firstOrNull()?.firstOrNull() ?: ""
        val dataRows = rows.drop(1) // ilk satırı at

        val entries = dataRows.map { row ->
            Language(
                wordOrSentence = row.getOrElse(0) { "" },
                meaning = row.getOrElse(1) { "" }
            )
        }

        emit(LanguageSet(setTitle, entries))
    } catch (e: Exception) {
        e.printStackTrace()
        emit(LanguageSet("", emptyList()))
    }
}.flowOn(Dispatchers.IO)