package com.lyadirga.bildirimleogren.model

import androidx.annotation.DrawableRes


data class Language(
    val wordOrSentence: String,
    val meaning: String,
    @DrawableRes
    val imageResId: Int? = null
)

data class LanguageSet(
    val title: String,
    val items: List<Language>
)
