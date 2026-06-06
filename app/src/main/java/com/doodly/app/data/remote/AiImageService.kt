package com.doodly.app.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AiImageService {
    @POST("v1beta/models/gemini-2.5-flash-image:generateContent")
    suspend fun generateImage(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiImageRequest
    ): GeminiImageResponse
}
