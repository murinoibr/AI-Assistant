package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class OpenAiApiServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: OpenAiApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenAiApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `chatCompletion success`() = runBlocking {
        // Arrange
        val responseJson = """
            {
              "id": "chatcmpl-123",
              "object": "chat.completion",
              "created": 1677652288,
              "model": "gpt-4o-mini",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": "Hello there, how may I assist you today?"
                  },
                  "finish_reason": "stop"
                }
              ],
              "usage": {
                "prompt_tokens": 9,
                "completion_tokens": 12,
                "total_tokens": 21
              }
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))

        val request = OpenAiChatRequest(
            messages = listOf(OpenAiMessage(role = "user", content = "Hello!"))
        )
        val authHeader = "Bearer sk-test-123"

        // Act
        val response = apiService.chatCompletion(authHeader, request)

        // Assert
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/v1/chat/completions", recordedRequest.path)
        assertEquals(authHeader, recordedRequest.getHeader("Authorization"))

        assertNotNull(response)
        assertEquals(1, response.choices?.size)
        assertEquals("assistant", response.choices?.get(0)?.message?.role)
        assertEquals("Hello there, how may I assist you today?", response.choices?.get(0)?.message?.content)
    }

    @Test
    fun `chatCompletion empty choices`() = runBlocking {
        // Arrange
        val responseJson = """
            {
              "choices": []
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))

        val request = OpenAiChatRequest(
            messages = listOf(OpenAiMessage(role = "user", content = "Hello!"))
        )

        // Act
        val response = apiService.chatCompletion("Bearer token", request)

        // Assert
        assertNotNull(response)
        assertEquals(0, response.choices?.size)
    }
}
