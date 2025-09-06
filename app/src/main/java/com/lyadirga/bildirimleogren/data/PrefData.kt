package com.lyadirga.bildirimleogren.data

import android.content.Context
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
        private val NOTIFICATION_INTERVAL_INDEX = intPreferencesKey("notification_interval_index")
        private val NOTIFICATION_SET_IDS = stringPreferencesKey("notification_set_ids")

        private const val DEFAULT_INDEX = -1
        const val NOTIFICATION_DISABLED_INDEX = 4 // Notification is off
    }

    // ✅ INDEX
    suspend fun resetIndex() {
        context.dataStore.edit { it[INDEX] = DEFAULT_INDEX }
    }

    suspend fun setIndex(index: Int) {
        context.dataStore.edit { it[INDEX] = index }
    }

    suspend fun getIndexOnce(): Int {
        return context.dataStore.data.first()[INDEX] ?: DEFAULT_INDEX
    }


    // ✅ Notification interval
    suspend fun setNotificationIntervalIndex(index: Int) {
        context.dataStore.edit { it[NOTIFICATION_INTERVAL_INDEX] = index }
    }

    suspend fun getNotificationIntervalIndexOnce(): Int {
        return context.dataStore.data.first()[NOTIFICATION_INTERVAL_INDEX] ?: NOTIFICATION_DISABLED_INDEX
    }

    // Bildirim açık olan set ID’lerini kaydet
    // Save the IDs of sets with notifications enabled
    suspend fun saveNotificationSetIds(setIds: List<Long>) {
        val jsonString = json.encodeToString(setIds)
        context.dataStore.edit { it[NOTIFICATION_SET_IDS] = jsonString }
    }

    // Observe as a flow
    fun observeNotificationSetIds(): Flow<List<Long>> {
        return context.dataStore.data.map { prefs ->
            val jsonString = prefs[NOTIFICATION_SET_IDS]
            if (jsonString.isNullOrEmpty()) emptyList()
            else json.decodeFromString(jsonString)
        }
    }

    // Get once
    // Tek seferlik al
    suspend fun getNotificationSetIdsOnce(): List<Long> {
        val jsonString = context.dataStore.data.first()[NOTIFICATION_SET_IDS]
        return if (jsonString.isNullOrEmpty()) emptyList()
        else json.decodeFromString(jsonString)
    }

    // Toggle mantığı (ekle/çıkar)
    // Toggle logic (add/remove)
    suspend fun toggleNotificationSetId(setId: Long) {
        val current = getNotificationSetIdsOnce().toMutableList()
        if (current.contains(setId)) {
            // zaten varsa çıkar
            // Remove if already exists
            current.remove(setId)
        } else {
            // yoksa ekle
            // Add if not exists
            current.add(setId)
        }
        saveNotificationSetIds(current)
    }


}
