package com.doodly.app.ui.write

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.doodly.app.data.local.Mood
import com.doodly.app.ui.component.MoodChip
import com.doodly.app.ui.component.TossButton
import com.doodly.app.ui.component.TossCard
import com.doodly.app.ui.theme.Lavender
import com.doodly.app.ui.theme.Peach
import com.doodly.app.ui.theme.SoftPink
import com.doodly.app.util.formatDate
import com.doodly.app.util.localDate
import com.doodly.app.util.normalizeDate
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun WriteScreen(
    viewModel: WriteViewModel,
    showCalendarBack: Boolean,
    onBackToCalendar: () -> Unit,
    onSaved: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.savedId) {
        state.savedId?.let(onSaved)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showCalendarBack) {
            TextButton(onClick = onBackToCalendar) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                Text("캘린더로", modifier = Modifier.padding(start = 6.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(SoftPink, Peach, Lavender)))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text("오늘은 어떤 하루였나요?", style = MaterialTheme.typography.headlineMedium)
            Text(
                "한두 문장이면 충분해요. 나머지는 Doodly가 그려드릴게요.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "하루 한 편 · 같은 날짜는 기존 기록을 수정해요.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        }

        TextButton(
            onClick = {
                val selected = localDate(state.date)
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        val date = LocalDate.of(year, month + 1, day)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        viewModel.setDate(normalizeDate(date))
                    },
                    selected.year,
                    selected.monthValue - 1,
                    selected.dayOfMonth
                ).apply {
                    datePicker.maxDate = System.currentTimeMillis()
                }.show()
            }
        ) {
            Icon(Icons.Outlined.CalendarMonth, null)
            Text(formatDate(state.date), modifier = Modifier.padding(start = 8.dp))
        }

        if (state.editingId != null) {
            Text(
                "이 날짜에 쓴 기록을 불러왔어요. 저장하면 기존 기록이 수정돼요.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Mood.entries.forEach { mood ->
                MoodChip(mood, state.mood == mood) { viewModel.setMood(mood) }
            }
        }

        OutlinedTextField(
            value = state.content,
            onValueChange = { viewModel.setContent(it.take(180)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            placeholder = { Text("예: 오랜만에 친구를 만나 많이 웃었다.") },
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface
            )
        )

        AnimatedContent(
            targetState = when {
                state.isLoadingEntry -> "loading"
                state.isGenerating -> "generating"
                state.imagePath != null -> state.imagePath.orEmpty()
                else -> "empty"
            },
            transitionSpec = {
                (fadeIn(tween(500)) + scaleIn(
                    initialScale = 0.86f,
                    animationSpec = spring(dampingRatio = 0.68f)
                )).togetherWith(fadeOut(tween(180)))
            },
            label = "artwork"
        ) { artworkState ->
            when (artworkState) {
                "loading" -> {
                    TossCard(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "기록을 불러오고 있어요",
                                modifier = Modifier.padding(top = 16.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                "generating" -> GeneratingArtworkCard()
                "empty" -> Unit
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AsyncImage(
                            model = File(artworkState),
                            contentDescription = "생성된 일기 그림",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(30.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "짠! 오늘의 그림이 완성됐어요.",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (state.imagePath == null) {
            TossButton(
                text = if (state.isGenerating) "그리는 중..." else "AI로 그림 그리기",
                onClick = viewModel::generateImage,
                enabled = state.content.isNotBlank() &&
                    !state.isGenerating &&
                    !state.isLoadingEntry
            )
        } else {
            TossButton(
                text = if (state.isSaving) "저장 중..."
                else if (state.editingId != null) "수정 내용 저장"
                else "저장",
                onClick = viewModel::save,
                enabled = !state.isSaving && !state.isLoadingEntry
            )
        }
    }
}

@Composable
private fun GeneratingArtworkCard() {
    val transition = rememberInfiniteTransition(label = "drawing")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_400),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkleRotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(850),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklePulse"
    )

    TossCard(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(SoftPink, Peach, Lavender))),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(104.dp)
                    .graphicsLayer {
                        rotationZ = rotation
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(36.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }
            Text(
                "오늘의 장면을 그리고 있어요",
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "잠시만 기다리면 그림이 짠 나타나요.",
                modifier = Modifier.padding(top = 7.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
