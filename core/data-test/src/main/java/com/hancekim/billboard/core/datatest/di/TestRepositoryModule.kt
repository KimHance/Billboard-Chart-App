package com.hancekim.billboard.core.datatest.di

import com.hancekim.billboard.core.data.repository.ChartRepository
import com.hancekim.billboard.core.dataimpl.di.RepositoryModule
import com.hancekim.billboard.core.datatest.repository.FakeChartRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn


@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class],
)
interface TestRepositoryModule {
    @Binds
    fun bindsChartRepository(
        fakeImpl: FakeChartRepository,
    ): ChartRepository
}