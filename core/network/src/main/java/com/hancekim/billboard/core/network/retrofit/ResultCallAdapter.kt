package com.hancekim.billboard.core.network.retrofit

import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ResultCallAdapter<T>(
    private val successType: Type,
    private val jsonBuilder: Json
) : CallAdapter<T, Call<Result<T>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<Result<T>> = ResultCall(call, jsonBuilder)
}

class ResultCallAdapterFactory(
    private val jsonBuilder: Json
) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != Result::class.java) return null

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return ResultCallAdapter<Any>(resultType, jsonBuilder)
    }
}