package com.doodly.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.Base64
import android.util.Log
import com.doodly.app.BuildConfig
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.local.Mood
import com.doodly.app.data.remote.AiImageService
import com.doodly.app.data.remote.GeminiImageRequest
import com.doodly.app.data.remote.GeminiRequestContent
import com.doodly.app.data.remote.GeminiRequestPart
import com.doodly.app.data.remote.GeminiTextService
import com.doodly.app.util.formatDate
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AiRepository(
    private val context: Context,
    private val imageService: AiImageService
) {
    suspend fun generateDiaryImage(content: String, mood: Mood): String {
        val key = BuildConfig.AI_KEY
        if (key.isBlank() || key == "DEFAULT_AI_KEY") {
            return createFallbackImage(mood)
        }

        return runCatching {
            val prompt = GeminiTextService(key)
                .createImagePrompt(content, mood.label)
                .imagePrompt
            val response = imageService.generateImage(
                apiKey = key,
                request = GeminiImageRequest(
                    contents = listOf(
                        GeminiRequestContent(
                            parts = listOf(GeminiRequestPart(prompt))
                        )
                    )
                )
            )
            val encoded = requireNotNull(response.firstBase64()) {
                "Image response did not contain base64 data."
            }
            saveBytes(Base64.decode(encoded, Base64.DEFAULT))
        }.getOrElse { throwable ->
            Log.w("DoodlyAI", "Image generation failed; using local fallback.", throwable)
            createFallbackImage(mood)
        }
    }

    suspend fun generateMoodInsight(
        entries: List<DiaryEntry>,
        fallback: String
    ): String {
        val key = BuildConfig.AI_KEY
        if (key.isBlank() || key == "DEFAULT_AI_KEY" || entries.size < 2) {
            return fallback
        }
        val summary = entries
            .sortedBy { it.date }
            .joinToString("\n") { entry ->
                "${formatDate(entry.date)} | ${Mood.fromName(entry.mood).label} | ${entry.content.take(60)}"
            }
        return runCatching {
            GeminiTextService(key).createMoodInsight(summary)
        }.getOrDefault(fallback)
    }

    private fun createFallbackImage(mood: Mood): String {
        val size = 1080
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val moodColor = Color.parseColor(mood.colorHex)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                size.toFloat(),
                size.toFloat(),
                lighten(moodColor, 0.48f),
                moodColor,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        paint.shader = null
        paint.color = Color.argb(45, 255, 255, 255)
        repeat(14) { index ->
            val radius = 40f + (index % 4) * 24f
            val x = ((index * 181) % size).toFloat()
            val y = ((index * 277) % size).toFloat()
            canvas.drawCircle(x, y, radius, paint)
        }
        val file = imageFile()
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return file.absolutePath
    }

    private fun saveBytes(bytes: ByteArray): String {
        val file = imageFile()
        file.writeBytes(bytes)
        return file.absolutePath
    }

    private fun imageFile(): File {
        val directory = File(context.filesDir, "images").apply { mkdirs() }
        return File(directory, "${UUID.randomUUID()}.png")
    }

    private fun lighten(color: Int, ratio: Float): Int {
        fun channel(value: Int) = (value + (255 - value) * ratio).toInt().coerceIn(0, 255)
        return Color.rgb(channel(Color.red(color)), channel(Color.green(color)), channel(Color.blue(color)))
    }
}
