package com.hancekim.billboard.core.network.retrofit

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject

interface NetworkFactory {

    fun <T> createNetworkService(
        service: Class<T>,
        url: String,
        interceptors: List<Interceptor> = emptyList(),
    ): T
}

class BillboardNetworkFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val resultCallAdapterFactory: ResultCallAdapterFactory,
    private val jsonBuilder: Json
) : NetworkFactory {
    override fun <T> createNetworkService(
        service: Class<T>,
        url: String,
        interceptors: List<Interceptor>,
    ): T = defaultRetrofitBuilder(url, interceptors).create(service)

    private fun defaultRetrofitBuilder(
        baseUrl: String,
        interceptors: List<Interceptor> = emptyList(),
    ): Retrofit {
        val client = if (interceptors.isEmpty()) {
            okHttpClient
        } else {
            okHttpClient.newBuilder().apply {
                interceptors.forEach { addInterceptor(it) }
            }.build()
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(jsonBuilder.asConverterFactory("application/json".toMediaType()))
            .addCallAdapterFactory(resultCallAdapterFactory)
            .build()
    }
}