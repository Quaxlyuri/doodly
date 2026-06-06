package com.doodly.app.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import com.doodly.app.data.local.DiaryEntry
import java.io.File
import java.io.FileOutputStream

object ShareHelper {
    fun share(
        context: Context,
        entry: DiaryEntry,
        includeDiaryText: Boolean
    ) {
        val source = entry.imagePath?.let(BitmapFactory::decodeFile)
            ?: Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
        val output = source.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = output.width * 0.035f
            setShadowLayer(6f, 0f, 2f, Color.argb(120, 0, 0, 0))
        }
        val watermark = if (includeDiaryText) {
            "Doodly · ${entry.content.take(42)}"
        } else {
            "Doodly"
        }
        canvas.drawText(watermark, 42f, output.height - 48f, paint)

        val directory = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(directory, "doodly_${entry.id}.png")
        FileOutputStream(file).use { output.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (output !== source) output.recycle()
        source.recycle()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (includeDiaryText) {
                putExtra(Intent.EXTRA_TEXT, entry.content)
            }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Doodly 기록 공유"))
    }
}
