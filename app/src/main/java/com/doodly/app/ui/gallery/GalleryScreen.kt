package com.doodly.app.ui.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.local.Mood
import com.doodly.app.ui.component.MoodIcon
import com.doodly.app.ui.component.TossCard
import com.doodly.app.util.formatDate
import java.io.File

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onEntryClick: (Int) -> Unit
) {
    val entries by viewModel.entries.collectAsState(initial = emptyList())
    var gridMode by rememberSaveable { androidx.compose.runtime.mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("기억하고 싶은 하루", style = MaterialTheme.typography.headlineLarge)
                Text(
                    "그림으로 다시 만나는 나의 기록",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${entries.size}",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .padding(horizontal = 13.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { gridMode = !gridMode }) {
                Icon(
                    if (gridMode) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                    contentDescription = "보기 방식 변경"
                )
            }
        }

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "아직 기록이 없어요.\n오늘의 한 문장을 남겨보세요.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (gridMode) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    GridEntry(entry) { onEntryClick(entry.id) }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(entries, key = { it.id }) { entry ->
                    ListEntry(entry) { onEntryClick(entry.id) }
                }
            }
        }
    }
}

@Composable
private fun GridEntry(entry: DiaryEntry, onClick: () -> Unit) {
    val mood = Mood.fromName(entry.mood)
    TossCard(onClick = onClick) {
        Box {
            AsyncImage(
                model = entry.imagePath?.let(::File),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            MoodIcon(
                mood = mood,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                size = 32.dp
            )
        }
        Text(
            formatDate(entry.date),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ListEntry(entry: DiaryEntry, onClick: () -> Unit) {
    val mood = Mood.fromName(entry.mood)
    TossCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.imagePath?.let(::File),
                contentDescription = null,
                modifier = Modifier.size(88.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoodIcon(mood = mood, size = 28.dp)
                    Text(formatDate(entry.date), style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    entry.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
