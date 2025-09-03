package com.lyadirga.bildirimleogren.data

import androidx.room.*

@Entity(
    tableName = "language_sets"
)
data class LanguageSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String? = null,
    val orderIndex: Int // setlerin sırasını korumak için
)

@Entity(
    tableName = "languages",
    foreignKeys = [
        ForeignKey(
            entity = LanguageSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("setId")]
)
data class LanguageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val setId: Long,
    val wordOrSentence: String,
    val meaning: String
)
