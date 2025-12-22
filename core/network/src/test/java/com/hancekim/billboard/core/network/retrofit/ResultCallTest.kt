package com.hancekim.billboard.core.network.retrofit

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertNotSame

class ResultCallTest {

    private val delegate = mockk<Call<String>>(relaxed = true)
    private val json = Json { ignoreUnknownKeys = true }
    private val resultCall = ResultCall(delegate, json)
    private val callback = mockk<Callback<Result<String>>>(relaxed = true)

    // ============================================
    // enqueue 테스트
    // ============================================

    @Test
    fun `성공 응답에 body가 있으면 Result_success로 감싸서 반환한다`() {
        val successBody = "success"
        val response = Response.success(successBody)

        val delegateCallbackSlot = slot<Callback<String>>()
        every { delegate.enqueue(capture(delegateCallbackSlot)) } answers {
            delegateCallbackSlot.captured.onResponse(delegate, response)
        }

        val resultSlot = slot<Response<Result<String>>>()
        every { callback.onResponse(any(), capture(resultSlot)) } returns Unit

        resultCall.enqueue(callback)

        val result = resultSlot.captured.body()
        assertTrue(result?.isSuccess == true)
        assertEquals(successBody, result?.getOrNull())
    }

    @Test
    fun `성공 응답이지만 body가 null이면 NullPointerException으로 실패 처리한다`() {
        val response = Response.success<String>(null)

        val delegateCallbackSlot = slot<Callback<String>>()
        every { delegate.enqueue(capture(delegateCallbackSlot)) } answers {
            delegateCallbackSlot.captured.onResponse(delegate, response)
        }

        val resultSlot = slot<Response<Result<String>>>()
        every { callback.onResponse(any(), capture(resultSlot)) } returns Unit

        resultCall.enqueue(callback)

        val result = resultSlot.captured.body()
        assertTrue(result?.isFailure == true)
        val exception = result?.exceptionOrNull()
        assertTrue(exception is NullPointerException)
        assertEquals("Response body is null", exception?.message)
    }

    @Test
    fun `HTTP 에러 응답에 ApiError JSON이 있으면 ApiException으로 파싱한다`() {
        val errorJson = """{"code":"INVALID_REQUEST","message":"잘못된 요청입니다"}"""

        val delegateCallbackSlot = slot<Callback<String>>()
        every { delegate.enqueue(capture(delegateCallbackSlot)) } answers {
            // 여기서 매번 새로 생성해야 errorBody가 소진 안 됨!
            val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())
            val response = Response.error<String>(400, errorBody)
            delegateCallbackSlot.captured.onResponse(delegate, response)
        }

        val resultSlot = slot<Response<Result<String>>>()
        every { callback.onResponse(any(), capture(resultSlot)) } returns Unit

        resultCall.enqueue(callback)

        val result = resultSlot.captured.body()
        assertTrue(result?.isFailure == true)
        val exception = result?.exceptionOrNull() as? ApiException
        assertNotNull(exception)
        assertEquals(400, exception?.httpCode)
        assertEquals("INVALID_REQUEST", exception?.apiError?.code)
        assertEquals("잘못된 요청입니다", exception?.apiError?.message)
    }

    @Test
    fun `HTTP 에러 응답의 body가 JSON 파싱 불가능하면 HttpException으로 처리한다`() {
        val invalidBody = "This is not JSON".toResponseBody("text/plain".toMediaTypeOrNull())
        val response = Response.error<String>(500, invalidBody)

        val delegateCallbackSlot = slot<Callback<String>>()
        every { delegate.enqueue(capture(delegateCallbackSlot)) } answers {
            delegateCallbackSlot.captured.onResponse(delegate, response)
        }

        val resultSlot = slot<Response<Result<String>>>()
        every { callback.onResponse(any(), capture(resultSlot)) } returns Unit

        resultCall.enqueue(callback)

        val result = resultSlot.captured.body()
        assertTrue(result?.isFailure == true)
        val exception = result?.exceptionOrNull()
        assertTrue(exception is HttpException)
        assertEquals(500, (exception as HttpException).code())
    }

    @Test
    fun `네트워크 실패시 onFailure가 호출되면 해당 Throwable로 실패 처리한다`() {
        val networkError = RuntimeException("Network unavailable")

        val delegateCallbackSlot = slot<Callback<String>>()
        every { delegate.enqueue(capture(delegateCallbackSlot)) } answers {
            delegateCallbackSlot.captured.onFailure(delegate, networkError)
        }

        val resultSlot = slot<Response<Result<String>>>()
        every { callback.onResponse(any(), capture(resultSlot)) } returns Unit

        resultCall.enqueue(callback)

        val result = resultSlot.captured.body()
        assertTrue(result?.isFailure == true)
        assertEquals(networkError, result?.exceptionOrNull())
    }

    // ============================================
    // execute 테스트
    // ============================================

    @Test
    fun `execute 호출시 UnsupportedOperationException이 발생한다`() {
        assertThrows(UnsupportedOperationException::class.java) {
            resultCall.execute()
        }
    }

    // ============================================
    // delegate 메서드 위임 테스트
    // ============================================

    @Test
    fun `clone 호출시 새로운 ResultCall을 반환한다`() {
        val clonedDelegate = mockk<Call<String>>(relaxed = true)
        every { delegate.clone() } returns clonedDelegate

        val cloned = resultCall.clone()

        assertNotSame(resultCall, cloned)
        verify { delegate.clone() }
    }

    @Test
    fun `isExecuted는 delegate의 상태를 반환한다`() {
        every { delegate.isExecuted } returns true
        assertTrue(resultCall.isExecuted)

        every { delegate.isExecuted } returns false
        assertFalse(resultCall.isExecuted)
    }

    @Test
    fun `isCanceled는 delegate의 상태를 반환한다`() {
        every { delegate.isCanceled } returns true
        assertTrue(resultCall.isCanceled)

        every { delegate.isCanceled } returns false
        assertFalse(resultCall.isCanceled)
    }

    @Test
    fun `cancel 호출시 delegate의 cancel이 호출된다`() {
        resultCall.cancel()
        verify { delegate.cancel() }
    }

    @Test
    fun `request는 delegate의 request를 반환한다`() {
        val mockRequest = mockk<Request>()
        every { delegate.request() } returns mockRequest

        assertEquals(mockRequest, resultCall.request())
    }

    @Test
    fun `timeout은 delegate의 timeout을 반환한다`() {
        val mockTimeout = mockk<Timeout>()
        every { delegate.timeout() } returns mockTimeout

        assertEquals(mockTimeout, resultCall.timeout())
    }
}
