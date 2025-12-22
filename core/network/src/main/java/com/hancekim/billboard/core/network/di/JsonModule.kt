package com.hancekim.billboard.core.network.di

import com.hancekim.billboard.core.network.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class JsonModule {
    @Provides
    @Singleton
    fun provideJsonBuilder(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
        if (BuildConfig.DEBUG) prettyPrint = true
    }
}