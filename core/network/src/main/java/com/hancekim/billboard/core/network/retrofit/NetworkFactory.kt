package com.hancekim.billboard.core.network.retrofit

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Inject

interface NetworkFactory {

    fun <T> createNetworkService(
        service: Class<T>,
        url: String,
    ): T
}

class BillboardNetworkFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val resultCallAdapterFactory: ResultCallAdapterFactory,
    private val jsonBuilder: Json
) : NetworkFactory {
    override fun <T> createNetworkService(
        service: Class<T>,
        url: String
    ): T = defaultRetrofitBuilder(url).create(service)

    private fun defaultRetrofitBuilder(
        baseUrl: String,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(jsonBuilder.asConverterFactory("application/json".toMediaType()))
        .addCallAdapterFactory(resultCallAdapterFactory)
        .build()
}