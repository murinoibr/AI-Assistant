package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<OpenAiMessage>,
    @param:Json(name = "temperature") val temperature: Float? = 0.7f
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    val message: OpenAiMessage?
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice>?
)

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiChatRequest
    ): OpenAiChatResponse
}

object OpenAiClient {
    private const val BASE_URL = "https://api.openai.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: OpenAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenAiApiService::class.java)
    }
}
