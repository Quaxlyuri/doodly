package com.doodly.app.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import org.json.JSONObject

data class ImagePrompt(val imagePrompt: String, val moodKeyword: String)

class GeminiTextService(apiKey: String) {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey,
        systemInstruction = content {
            text(
                """
                너는 일기를 그림으로 표현하기 위한 아트 디렉터다.
                사용자의 짧은 일기와 무드를 받아, 따뜻하고 귀여운
                손그림(doodle/illustration) 스타일의 영어 이미지 생성 프롬프트를 만든다.
                출력은 반드시 아래 JSON 형식만 출력한다(설명/마크다운 금지):
                {"image_prompt": "...", "mood_keyword": "..."}
                """.trimIndent()
            )
        }
    )
    private val moodModel = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey,
        systemInstruction = content {
            text(
                """
                너는 다정하지만 과장하지 않는 감정 기록 도우미다.
                최근 일기와 감정 흐름을 보고 한국어로 짧은 한마디를 한다.
                진단하거나 단정하지 말고, 45자 이내 한 문장만 출력한다.
                이모지, 마크다운, 따옴표는 사용하지 않는다.
                """.trimIndent()
            )
        }
    )

    suspend fun createImagePrompt(content: String, mood: String): ImagePrompt {
        val response = generativeModel.generateContent("Diary: $content\nMood: $mood")
        val raw = response.text.orEmpty()
        val jsonText = raw.substringAfter('{', "").substringBeforeLast('}', "")
        require(jsonText.isNotBlank()) { "Gemini did not return JSON." }
        val json = JSONObject("{$jsonText}")
        return ImagePrompt(
            imagePrompt = json.getString("image_prompt"),
            moodKeyword = json.optString("mood_keyword", mood)
        )
    }

    suspend fun createMoodInsight(summary: String): String {
        val response = moodModel.generateContent(
            "최근 7일 감정 기록:\n$summary\n변화를 짚고 다정한 한마디를 해줘."
        )
        return response.text.orEmpty()
            .replace(Regex("[\"*_`#]"), "")
            .trim()
            .lineSequence()
            .firstOrNull()
            .orEmpty()
            .take(70)
            .also { require(it.isNotBlank()) { "Gemini did not return an insight." } }
    }
}
