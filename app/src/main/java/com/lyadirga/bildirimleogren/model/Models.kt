package com.lyadirga.bildirimleogren.model


data class Language(
    val wordOrSentence: String,
    val meaning: String
)

data class LanguageSet(
    val title: String,
    val items: List<Language>,
    val url: String? = null
)
