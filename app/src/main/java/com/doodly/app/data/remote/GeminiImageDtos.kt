package com.doodly.app.data.remote

data class GeminiImageRequest(
    val contents: List<GeminiRequestContent>,
    val generationConfig: GeminiImageGenerationConfig =
        GeminiImageGenerationConfig()
)

data class GeminiRequestContent(
    val parts: List<GeminiRequestPart>
)

data class GeminiRequestPart(
    val text: String
)

data class GeminiImageGenerationConfig(
    val responseModalities: List<String> = listOf("TEXT", "IMAGE")
)

data class GeminiImageResponse(
    val candidates: List<GeminiCandidate>? = null
) {
    fun firstBase64(): String? = candidates
        .orEmpty()
        .flatMap { it.content?.parts.orEmpty() }
        .firstNotNullOfOrNull { it.inlineData?.data }
}

data class GeminiCandidate(
    val content: GeminiResponseContent? = null
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>? = null
)

data class GeminiResponsePart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mimeType: String? = null,
    val data: String? = null
)
