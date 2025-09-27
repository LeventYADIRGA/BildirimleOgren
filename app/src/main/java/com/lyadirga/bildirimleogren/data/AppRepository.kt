
package com.lyadirga.bildirimleogren.data

import com.lyadirga.bildirimleogren.model.LanguageSet
import com.lyadirga.bildirimleogren.model.LanguageSetSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.collections.forEachIndexed

class AppRepository @Inject constructor(
    private val dao: LanguageDao
) {

    suspend fun insertOrUpdateSets(languageSets: List<LanguageSet>) {
        // Mevcut setleri DB'den al (URL ile eşleştireceğiz)
        // Get existing sets from DB (to match with URL)
        val existingSets = dao.getAllLanguageSetsWithItems().associateBy { it.set.url }
        val maxOrderIndex = existingSets.values.maxOfOrNull { it.set.orderIndex } ?: -1
        var currentMaxOrderIndex = maxOrderIndex

        languageSets.forEachIndexed { newIndex, newSet ->

            val existing = existingSets[newSet.url]
            val orderIndex = existing?.set?.orderIndex ?: (++currentMaxOrderIndex)


            // Add the set or update if it exists
            val setId = dao.insertLanguageSet(
                LanguageSetEntity(
                    id = existing?.set?.id ?: 0, // Update if exists, otherwise insert
                    title = newSet.title,
                    url = newSet.url,
                    orderIndex = orderIndex
                )
            )

            // Bu setin eski kelimelerini sil
            // Delete the old words of this set
            dao.deleteLanguagesBySetId(setId)

            // Yeni kelimeleri sırayla ekle
            // Add new words in order
            val entities = newSet.items.mapIndexed { index, lang ->
                LanguageEntity(
                    setId = setId,
                    wordOrSentence = lang.wordOrSentence,
                    meaning = lang.meaning
                )
            }
            dao.insertLanguages(entities)
        }
    }

    fun getAllSetSummariesFlow(): Flow<List<LanguageSetSummary>> {
        return dao.getAllSetSummariesFlow()
    }

    suspend fun getAllSetSummariesOnce(): List<LanguageSetSummary> {
        return dao.getAllSetSummariesFlow().first()
    }


    suspend fun getSetById(setId: Long): LanguageSetWithItems? {
        return dao.getLanguageSetWithItemsById(setId)
    }

    fun getAllSetsFlow(): Flow<List<LanguageSetWithItems>> {
        return dao.getAllLanguageSetsWithItemsFlow()
    }

    suspend fun getSetsByIds(setIds: List<Long>): List<LanguageSetWithItems> {
        return dao.getLanguageSetsWithItemsByIds(setIds)
    }

    suspend fun deleteSetById(setId: Long) {
        dao.deleteLanguageSetById(setId)
    }

}
