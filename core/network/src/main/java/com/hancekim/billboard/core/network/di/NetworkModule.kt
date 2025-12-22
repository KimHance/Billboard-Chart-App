package com.hancekim.billboard.core.network.di

import com.hancekim.billboard.core.network.BuildConfig
import com.hancekim.billboard.core.network.retrofit.BillboardNetworkFactory
import com.hancekim.billboard.core.network.retrofit.NetworkFactory
import com.hancekim.billboard.core.network.retrofit.ResultCallAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindNetworkFactory(impl: BillboardNetworkFactory): NetworkFactory

    companion object {

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()

        @Provides
        @Singleton
        fun provideNetworkResultCallAdapter(
            jsonBuilder: Json
        ): ResultCallAdapterFactory = ResultCallAdapterFactory(jsonBuilder)
    }
}