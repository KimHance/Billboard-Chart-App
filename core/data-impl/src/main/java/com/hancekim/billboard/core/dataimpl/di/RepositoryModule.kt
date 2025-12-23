package com.hancekim.billboard.core.dataimpl.di

import com.hancekim.billboard.core.data.repository.ChartRepository
import com.hancekim.billboard.core.dataimpl.repository.ChartRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindChartRepository(impl: ChartRepositoryImpl): ChartRepository
}