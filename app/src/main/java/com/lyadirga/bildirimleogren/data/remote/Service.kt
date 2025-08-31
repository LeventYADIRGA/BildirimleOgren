package com.lyadirga.bildirimleogren.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URL

val SHEET_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTO8rQRuzuW4R1t94Bpfd-Dsc8sR-ZZdlGiAvoFmP9_61bgaaHsOU2GynonalYSc-bdNHNjwqIMG20p/pub?gid=0&single=true&output=csv"
val SHEET_URL2 = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTO8rQRuzuW4R1t94Bpfd-Dsc8sR-ZZdlGiAvoFmP9_61bgaaHsOU2GynonalYSc-bdNHNjwqIMG20p/pub?output=csv"
val SSS = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTO8rQRuzuW4R1t94Bpfd-Dsc8sR-ZZdlGiAvoFmP9_61bgaaHsOU2GynonalYSc-bdNHNjwqIMG20p/pub?gid=1239646743&single=true&output=csv"


// CSV’yi internetten çekip parse eden Flow fonksiyonu, WordSet döner
fun fetchSheetCsvFlow(url: String): Flow<WordSet> = flow {
    try {
        // CSV verisini çek
        val csvText = URL(url).readText()

        // Satırları ayır, boş satırları at
        val rows = csvText.lines()
            .filter { it.isNotBlank() }
            .map { it.split(",") }

        // Parse et
        val setTitle = rows.firstOrNull()?.firstOrNull() ?: ""
        val dataRows = rows.drop(1)
        val entries = dataRows.map { row ->
            WordEntry(
                word = row.getOrElse(0) { "" },
                meaning = row.getOrElse(1) { "" }
            )
        }

        emit(WordSet(setTitle, entries)) // WordSet olarak emit
    } catch (e: Exception) {
        e.printStackTrace()
        emit(WordSet("", emptyList())) // hata durumunda boş WordSet
    }
}.flowOn(Dispatchers.IO) // IO thread’te çalışır


data class WordSet(
    val setTitle: String,
    val entries: List<WordEntry>
)

data class WordEntry(
    val word: String,
    val meaning: String
)
