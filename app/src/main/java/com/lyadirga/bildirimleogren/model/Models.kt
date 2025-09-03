package com.lyadirga.bildirimleogren.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val wordOrSentence: String,
    val meaning: String
)

@Serializable
data class LanguageSet(
    val title: String,
    val items: List<Language>,
    val url: String? = null
)
