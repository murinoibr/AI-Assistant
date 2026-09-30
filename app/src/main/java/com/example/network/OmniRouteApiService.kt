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
data class OmniRouteMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OmniRouteChatRequest(
    val model: String = "deepseek-v4.1-flash",
    val messages: List<OmniRouteMessage>,
    @param:Json(name = "temperature") val temperature: Float? = 0.7f
)

@JsonClass(generateAdapter = true)
data class OmniRouteChoice(
    val message: OmniRouteMessage? = null
)

@JsonClass(generateAdapter = true)
data class OmniRouteChatResponse(
    val choices: List<OmniRouteChoice>? = null
)

interface OmniRouteApiService {
    // OpenAI-compatible standard chat completions endpoint
    @POST("v1/chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authHeader: String,
        @Body request: OmniRouteChatRequest
    ): OmniRouteChatResponse

    // OpenCode / Zen alternative path support
    @POST("zen/v1/chat/completions")
    suspend fun zenChatCompletions(
        @Header("Authorization") authHeader: String,
        @Body request: OmniRouteChatRequest
    ): OmniRouteChatResponse
}

object OmniRouteClient {
    // Primary OmniRoute Gateway Host
    const val DEFAULT_BASE_URL = "https://opencode.ai/"
    const val FALLBACK_BASE_URL = "https://api.openai.com/"

    const val DEFAULT_OMNIROUTE_KEY = "oc_sk_e072daec3827_30l4GdwozfThJTLI0KRlYxUQBOgE1wgW"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: OmniRouteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OmniRouteApiService::class.java)
    }

    val openAiCompatibleService: OmniRouteApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FALLBACK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OmniRouteApiService::class.java)
    }
}
