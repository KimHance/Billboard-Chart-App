package com.hancekim.billboard.core.datasource.di

import com.hancekim.billboard.core.datasource.service.BillboardService
import com.hancekim.billboard.core.datasource.service.YoutubeService
import com.hancekim.billboard.core.network.retrofit.BillboardNetworkFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkServiceModule {

    private const val YOUTUBE_API_KEY = "AIzaSyDZFdZRwT3vG3Nu5RGTgcH4omdx-g0jmRw"

    @Provides
    @Singleton
    fun provideBillboardService(networkFactory: BillboardNetworkFactory): BillboardService =
        networkFactory
            .createNetworkService(
                service = BillboardService::class.java,
                url = "https://KimHance.github.io/Billboard-Auto-Scarping/"
            )

    @Provides
    @Singleton
    fun provideYoutubeService(networkFactory: BillboardNetworkFactory): YoutubeService =
        networkFactory
            .createNetworkService(
                service = YoutubeService::class.java,
                url = "https://www.googleapis.com/youtube/v3/",
                interceptors = listOf(
                    Interceptor { chain ->
                        val original = chain.request()
                        val url = original.url.newBuilder()
                            .addQueryParameter("key", YOUTUBE_API_KEY)
                            .build()
                        chain.proceed(original.newBuilder().url(url).build())
                    }
                )
            )
}
