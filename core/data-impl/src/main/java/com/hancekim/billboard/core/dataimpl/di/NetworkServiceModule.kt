package com.hancekim.billboard.core.dataimpl.di

import com.hancekim.billboard.core.dataimpl.service.BillboardService
import com.hancekim.billboard.core.network.retrofit.BillboardNetworkFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkServiceModule {

    @Provides
    @Singleton
    fun provideBillboardService(networkFactory: BillboardNetworkFactory): BillboardService =
        networkFactory
            .createNetworkService(
                service = BillboardService::class.java,
                url = "https://KimHance.github.io/Billboard-Auto-Scarping"
            )
}