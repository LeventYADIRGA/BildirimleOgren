package com.lyadirga.bildirimleogren.model

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val wordOrSentence: String,
    val meaning: String,
    @DrawableRes
    val imageResId: Int? = null
)

@Serializable
data class LanguageSet(
    val title: String,
    val items: List<Language>
)
