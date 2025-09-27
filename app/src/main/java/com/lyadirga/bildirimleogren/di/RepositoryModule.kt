package com.lyadirga.bildirimleogren.di

import com.lyadirga.bildirimleogren.data.repository.AppRepository
import com.lyadirga.bildirimleogren.data.repository.Repository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindRepository(repository: AppRepository): Repository
}