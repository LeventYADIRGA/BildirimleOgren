package com.lyadirga.bildirimleogren.data

import androidx.room.*
import com.lyadirga.bildirimleogren.model.LanguageSetSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguageSet(set: LanguageSetEntity): Long

    @Query("DELETE FROM ${LanguageSetEntity.TABLE_NAME} WHERE id = :setId")
    suspend fun deleteLanguageSetById(setId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(languages: List<LanguageEntity>)

    @Transaction
    @Query("SELECT * FROM ${LanguageSetEntity.TABLE_NAME} ORDER BY orderIndex ASC")
    suspend fun getAllLanguageSetsWithItems(): List<LanguageSetWithItems>

    @Query("DELETE FROM ${LanguageEntity.TABLE_NAME} WHERE setId = :setId")
    suspend fun deleteLanguagesBySetId(setId: Long)

    @Transaction
    @Query("SELECT * FROM ${LanguageSetEntity.TABLE_NAME} ORDER BY orderIndex ASC")
    fun getAllLanguageSetsWithItemsFlow(): Flow<List<LanguageSetWithItems>>

    @Query("SELECT id, title, url FROM ${LanguageSetEntity.TABLE_NAME} ORDER BY orderIndex ASC")
    fun getAllSetSummariesFlow(): Flow<List<LanguageSetSummary>>

    @Transaction
    @Query("SELECT * FROM ${LanguageSetEntity.TABLE_NAME} WHERE id = :setId")
    suspend fun getLanguageSetWithItemsById(setId: Long): LanguageSetWithItems?

    @Query("DELETE FROM ${LanguageSetEntity.TABLE_NAME}")
    suspend fun deleteAllSets()

    @Query("DELETE FROM ${LanguageEntity.TABLE_NAME}")
    suspend fun deleteAllLanguages()

    @Transaction
    suspend fun clearDatabase() {
        deleteAllLanguages()
        deleteAllSets()
    }

}

data class LanguageSetWithItems(
    @Embedded val set: LanguageSetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "setId",
        entity = LanguageEntity::class
    )
    val items: List<LanguageEntity>
)
