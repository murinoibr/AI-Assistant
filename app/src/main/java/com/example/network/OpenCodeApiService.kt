package com.example.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenCodeChatRequest(
    val model: String = "deepseek-v4.1-flash",
    val messages: List<OpenCodeMessage>
)

@JsonClass(generateAdapter = true)
data class OpenCodeMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenCodeChatResponse(
    val choices: List<OpenCodeChoice>? = null
)

@JsonClass(generateAdapter = true)
data class OpenCodeChoice(
    val message: OpenCodeMessage? = null
)

interface OpenCodeApiService {
    @POST("zen/v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authHeader: String,
        @Body request: OpenCodeChatRequest
    ): OpenCodeChatResponse
}

object OpenCodeClient {
    private const val BASE_URL = "https://opencode.ai/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: OpenCodeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(GeminiClient.moshi))
            .build()
            .create(OpenCodeApiService::class.java)
    }
}
