package com.lyadirga.bildirimleogren.data

import androidx.room.*

@Entity(
    tableName = LanguageSetEntity.TABLE_NAME
)
data class LanguageSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String? = null,
    // setlerin sırasını korumak için
    // To preserve the order of the sets
    val orderIndex: Int
) {
    companion object {
        const val TABLE_NAME = "language_sets"
    }
}

@Entity(
    tableName = LanguageEntity.TABLE_NAME,
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
){
    companion object {
        const val TABLE_NAME = "languages"
    }
}
