package com.hancekim.billboard.core.datasource.di

import com.hancekim.billboard.core.datasource.ChartDataSource
import com.hancekim.billboard.core.datasource.ChartDataSourceImpl
import com.hancekim.billboard.core.datasource.PreferenceDataSource
import com.hancekim.billboard.core.datasource.PreferenceDataSourceImpl
import com.hancekim.billboard.core.datasource.YoutubeDataSource
import com.hancekim.billboard.core.datasource.YoutubeDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Binds
    @Singleton
    abstract fun bindChartDataSource(impl: ChartDataSourceImpl): ChartDataSource

    @Binds
    @Singleton
    abstract fun bindYoutubeDataSource(impl: YoutubeDataSourceImpl): YoutubeDataSource

    @Binds
    @Singleton
    abstract fun bindPreferenceDataSource(impl: PreferenceDataSourceImpl): PreferenceDataSource
}
