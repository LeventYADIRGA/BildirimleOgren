package com.lyadirga.bildirimleogren.model

data class LanguageSet(
    val title: String,
    val items: List<Language>,
    val url: String? = null
)
