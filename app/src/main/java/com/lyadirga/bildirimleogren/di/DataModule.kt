package com.lyadirga.bildirimleogren.di

import android.content.Context
import androidx.room.Room
import com.lyadirga.bildirimleogren.data.AppDatabase
import com.lyadirga.bildirimleogren.data.LanguageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideLanguageDao(db: AppDatabase): LanguageDao {
        return db.languageDao()
    }
}
