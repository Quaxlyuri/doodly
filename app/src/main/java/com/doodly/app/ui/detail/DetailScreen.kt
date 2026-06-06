package com.doodly.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.doodly.app.data.local.Mood
import com.doodly.app.share.ShareHelper
import com.doodly.app.ui.component.MoodIcon
import com.doodly.app.ui.component.TossButton
import com.doodly.app.ui.component.TossCard
import com.doodly.app.util.formatDate
import java.io.File

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val entry by viewModel.entry.collectAsState()
    val context = LocalContext.current
    var includeDiaryText by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "뒤로")
            }
            Text(
                "기록",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { viewModel.delete(onBack) }) {
                Icon(Icons.Outlined.Delete, "삭제", tint = MaterialTheme.colorScheme.error)
            }
        }

        val current = entry
        if (current == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            val mood = Mood.fromName(current.mood)
            AsyncImage(
                model = current.imagePath?.let(::File),
                contentDescription = "일기 그림",
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier.padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoodIcon(mood = mood, size = 32.dp)
                Text(mood.label, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                formatDate(current.date),
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                current.content,
                modifier = Modifier.padding(top = 18.dp, bottom = 20.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            TossCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("일기 글도 함께 공유", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (includeDiaryText) "그림과 공유 메시지에 글을 포함해요."
                            else "그림만 깔끔하게 공유해요.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = includeDiaryText,
                        onCheckedChange = { includeDiaryText = it }
                    )
                }
            }
            TossButton(
                text = if (includeDiaryText) "그림과 글 공유하기" else "그림만 공유하기",
                onClick = {
                    ShareHelper.share(
                        context = context,
                        entry = current,
                        includeDiaryText = includeDiaryText
                    )
                },
                modifier = Modifier.padding(top = 14.dp, bottom = 24.dp)
            )
        }
    }
}
