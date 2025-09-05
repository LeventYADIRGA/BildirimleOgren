package com.lyadirga.bildirimleogren.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "Preferences")

class PrefData @Inject constructor(@ApplicationContext private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val INDEX = intPreferencesKey("index")
        private val CALISMA_SETI = intPreferencesKey("calisma_seti")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val NOTIFICATION_INTERVAL_INDEX = intPreferencesKey("notification_interval_index")
        private val NOTIFICATION_SET_IDS = stringPreferencesKey("notification_set_ids")

        private const val DEFAULT_INDEX = -1
        private const val DEFAULT_CALISMA_SETI = 0
        const val NOTIFICATION_DISABLED_INDEX = 4 // Bildirim kapalı
    }

    // ✅ INDEX
    suspend fun resetIndex() {
        context.dataStore.edit { it[INDEX] = DEFAULT_INDEX }
    }

    suspend fun setIndex(index: Int) {
        context.dataStore.edit { it[INDEX] = index }
    }

    fun observeIndex(): Flow<Int> {
        return context.dataStore.data.map { it[INDEX] ?: DEFAULT_INDEX }
    }

    suspend fun getIndexOnce(): Int {
        return context.dataStore.data.first()[INDEX] ?: DEFAULT_INDEX
    }

    // ✅ Çalışma seti
    suspend fun setCalismaSeti(index: Int) {
        context.dataStore.edit { it[CALISMA_SETI] = index }
    }

    fun observeCalismaSeti(): Flow<Int> {
        return context.dataStore.data.map { it[CALISMA_SETI] ?: DEFAULT_CALISMA_SETI }
    }

    suspend fun getCalismaSetiOnce(): Int {
        return context.dataStore.data.first()[CALISMA_SETI] ?: DEFAULT_CALISMA_SETI
    }

    // ✅ İlk açılış
    suspend fun setFirstLaunch(value: Boolean) {
        context.dataStore.edit { it[IS_FIRST_LAUNCH] = value }
    }

    fun observeFirstLaunch(): Flow<Boolean> {
        return context.dataStore.data.map { it[IS_FIRST_LAUNCH] ?: true }
    }

    suspend fun isFirstLaunchOnce(): Boolean {
        return context.dataStore.data.first()[IS_FIRST_LAUNCH] ?: true
    }

    // ✅ Bildirim interval
    suspend fun setNotificationIntervalIndex(index: Int) {
        context.dataStore.edit { it[NOTIFICATION_INTERVAL_INDEX] = index }
    }

    fun observeNotificationIntervalIndex(): Flow<Int> {
        return context.dataStore.data.map { it[NOTIFICATION_INTERVAL_INDEX] ?: 0 }
    }

    suspend fun getNotificationIntervalIndexOnce(): Int {
        return context.dataStore.data.first()[NOTIFICATION_INTERVAL_INDEX] ?: NOTIFICATION_DISABLED_INDEX
    }

    // Bildirim açık olan set ID’lerini kaydet
    suspend fun saveNotificationSetIds(setIds: List<Long>) {
        val jsonString = json.encodeToString(setIds)
        context.dataStore.edit { it[NOTIFICATION_SET_IDS] = jsonString }
    }

    // Akış olarak gözlemle
    fun observeNotificationSetIds(): Flow<List<Long>> {
        return context.dataStore.data.map { prefs ->
            val jsonString = prefs[NOTIFICATION_SET_IDS]
            if (jsonString.isNullOrEmpty()) emptyList()
            else json.decodeFromString(jsonString)
        }
    }

    // Tek seferlik al
    suspend fun getNotificationSetIdsOnce(): List<Long> {
        val jsonString = context.dataStore.data.first()[NOTIFICATION_SET_IDS]
        return if (jsonString.isNullOrEmpty()) emptyList()
        else json.decodeFromString(jsonString)
    }

    // Toggle mantığı (ekle/çıkar)
    suspend fun toggleNotificationSetId(setId: Long) {
        val current = getNotificationSetIdsOnce().toMutableList()
        if (current.contains(setId)) {
            current.remove(setId) // zaten varsa çıkar
        } else {
            current.add(setId) // yoksa ekle
        }
        saveNotificationSetIds(current)
    }


}
