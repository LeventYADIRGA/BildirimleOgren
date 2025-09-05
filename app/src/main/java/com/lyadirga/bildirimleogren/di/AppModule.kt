package com.lyadirga.bildirimleogren.di

import android.content.Context
import androidx.room.Room
import com.lyadirga.bildirimleogren.data.AppDatabase
import com.lyadirga.bildirimleogren.data.LanguageDao
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.data.Repository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideLanguageDao(db: AppDatabase): LanguageDao {
        return db.languageDao()
    }

    @Provides
    @Singleton
    fun provideRepository(dao: LanguageDao): Repository {
        return Repository(dao)
    }

    @Provides
    @Singleton
    fun providePrefData(@ApplicationContext context: Context): PrefData {
        return PrefData(context)
    }
}
