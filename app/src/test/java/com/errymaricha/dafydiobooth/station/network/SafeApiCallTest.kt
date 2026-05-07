package com.errymaricha.dafydiobooth.station.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SafeApiCallTest {

    @Test
    fun `maps 401 to unauthorized`() = runTest {
        val result = safeApiCall<String> {
            throw HttpException(Response.error<String>(401, "{}".toResponseBody("application/json".toMediaType())))
        }
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Unauthorized)
    }

    @Test
    fun `maps 422 to validation`() = runTest {
        val result = safeApiCall<String> {
            throw HttpException(
                Response.error<String>(
                    422,
                    "{\"message\":\"Validation failed\"}".toResponseBody("application/json".toMediaType()),
                ),
            )
        }
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Validation)
    }

    @Test
    fun `maps 500 to server`() = runTest {
        val result = safeApiCall<String> {
            throw HttpException(
                Response.error<String>(
                    500,
                    "{\"message\":\"Server error\"}".toResponseBody("application/json".toMediaType()),
                ),
            )
        }
        assertTrue(result is AppResult.Failure)
        assertTrue((result as AppResult.Failure).error is AppError.Server)
    }
}
