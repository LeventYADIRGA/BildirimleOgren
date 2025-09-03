package com.lyadirga.bildirimleogren.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LanguageSetEntity::class, LanguageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun languageDao(): LanguageDao
}
