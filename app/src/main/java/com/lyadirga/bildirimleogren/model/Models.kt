package com.lyadirga.bildirimleogren.model

data class LearningSet(
    var header: String = "",
    var data: MutableList<Sentence> = mutableListOf()
)

data class Sentence(
    val wordOrSentence: String = "",
    val meaning: String = ""
)