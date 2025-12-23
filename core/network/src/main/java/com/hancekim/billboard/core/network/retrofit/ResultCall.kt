package com.hancekim.billboard.core.network.retrofit

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class ResultCall<T>(
    private val delegate: Call<T>,
    private val jsonBuilder: Json
) : Call<Result<T>> {

    override fun execute(): Response<Result<T>> {
        throw UnsupportedOperationException("동기 호출 미지원")
    }

    override fun enqueue(callback: Callback<Result<T>>) {
        delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val result = if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(NullPointerException("Response body is null"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val exception = try {
                        errorBody?.let {
                            val apiError = jsonBuilder.decodeFromString(ApiError.serializer(), it)
                            ApiException(response.code(), apiError)
                        } ?: HttpException(response)
                    } catch (e: Exception) {
                        HttpException(response)
                    }
                    Result.failure(exception)
                }
                callback.onResponse(this@ResultCall, Response.success(result))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@ResultCall, Response.success(Result.failure(t)))
            }
        })
    }

    override fun clone(): Call<Result<T>> = ResultCall(delegate.clone(), jsonBuilder)
    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun isCanceled(): Boolean = delegate.isCanceled
    override fun cancel() = delegate.cancel()
    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
}

@Serializable
data class ApiError(
    val code: String? = null,
    val message: String? = null
)

class ApiException(
    val httpCode: Int,
    val apiError: ApiError
) : Exception(apiError.message ?: "Unknown error")