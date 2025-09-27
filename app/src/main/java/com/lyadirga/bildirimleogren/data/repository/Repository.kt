package com.lyadirga.bildirimleogren.data.repository

import com.lyadirga.bildirimleogren.data.LanguageSetWithItems
import com.lyadirga.bildirimleogren.model.LanguageSet
import com.lyadirga.bildirimleogren.model.LanguageSetSummary
import kotlinx.coroutines.flow.Flow

interface Repository {

    suspend fun insertOrUpdateSets(languageSets: List<LanguageSet>)

    fun getAllSetSummariesFlow(): Flow<List<LanguageSetSummary>>

    suspend fun getAllSetSummariesOnce(): List<LanguageSetSummary>

    suspend fun getSetById(setId: Long): LanguageSetWithItems?

    fun getAllSetsFlow(): Flow<List<LanguageSetWithItems>>

    suspend fun getSetsByIds(setIds: List<Long>): List<LanguageSetWithItems>

    suspend fun deleteSetById(setId: Long)
}
