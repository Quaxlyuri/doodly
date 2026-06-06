package com.doodly.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.local.Mood
import com.doodly.app.ui.component.MoodIcon
import com.doodly.app.ui.theme.Lavender
import com.doodly.app.ui.theme.Peach
import com.doodly.app.ui.theme.SoftPink
import com.doodly.app.util.dateMillis
import com.doodly.app.util.localDate
import java.io.File
import java.time.YearMonth
import java.time.LocalDate

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onEntryClick: (Int) -> Unit,
    onEmptyDateClick: (Long) -> Unit
) {
    val month by viewModel.month.collectAsState()
    val entries by viewModel.entries.collectAsState(initial = emptyList())
    val insight by viewModel.moodInsight.collectAsState()
    val today = LocalDate.now()
    val entriesByDay = entries
        .filter { localDate(it.date) <= today }
        .associateBy { localDate(it.date).dayOfMonth }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "나의 마음 달력",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 22.dp)
        )
        Text(
            "하루씩 쌓인 마음을 천천히 돌아봐요.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
        )

        MoodInsightCard(insight)

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "이전 달")
            }
            Text(
                "${month.year}년 ${month.monthValue}월",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(
                onClick = viewModel::nextMonth,
                enabled = month < YearMonth.now()
            ) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "다음 달")
            }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        MonthGrid(
            month = month,
            entriesByDay = entriesByDay,
            onEntryClick = onEntryClick,
            onEmptyDateClick = onEmptyDateClick
        )
        Box(Modifier.height(24.dp))
    }
}

@Composable
private fun MoodInsightCard(state: MoodInsightUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    listOf(SoftPink, Peach.copy(alpha = 0.78f), Lavender.copy(alpha = 0.8f))
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.72f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("요즘 내 감정 보기", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "최근 7일 · ${state.recordCount}일 기록",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                state.dominantMood?.let { MoodIcon(it, size = 36.dp) }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                state.days.forEach { day ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            weekdayLabel(day.date.dayOfWeek.value),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (day.mood != null) {
                            MoodIcon(day.mood, size = 31.dp)
                        } else {
                            Box(
                                Modifier
                                    .size(31.dp)
                                    .background(Color.White.copy(alpha = 0.52f), CircleShape)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
                    .padding(15.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isAiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    state.message,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    entriesByDay: Map<Int, DiaryEntry>,
    onEntryClick: (Int) -> Unit,
    onEmptyDateClick: (Long) -> Unit
) {
    val leading = month.atDay(1).dayOfWeek.value - 1
    val total = leading + month.lengthOfMonth()
    val cellCount = if (total <= 35) 35 else 42
    Layout(
        modifier = Modifier.fillMaxWidth().height(if (cellCount == 35) 380.dp else 456.dp),
        content = {
            repeat(cellCount) { index ->
                val day = index - leading + 1
                if (day in 1..month.lengthOfMonth()) {
                    val date = month.atDay(day)
                    DayCell(
                        day = day,
                        entry = entriesByDay[day],
                        enabled = date <= LocalDate.now(),
                        onClick = {
                            val entry = entriesByDay[day]
                            if (entry != null) onEntryClick(entry.id)
                            else onEmptyDateClick(dateMillis(date))
                        }
                    )
                } else {
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    ) { measurables, constraints ->
        val cellWidth = constraints.maxWidth / 7
        val rows = cellCount / 7
        val cellHeight = constraints.maxHeight / rows
        val placeables = measurables.map {
            it.measure(
                constraints.copy(
                    minWidth = cellWidth,
                    maxWidth = cellWidth,
                    minHeight = cellHeight,
                    maxHeight = cellHeight
                )
            )
        }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                placeable.placeRelative((index % 7) * cellWidth, (index / 7) * cellHeight)
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    entry: DiaryEntry?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val mood = entry?.let { Mood.fromName(it.mood) }
    Box(
        modifier = Modifier
            .padding(3.dp)
            .fillMaxSize()
            .alpha(if (enabled) 1f else 0.34f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (entry == null) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        entry?.imagePath?.let {
            AsyncImage(
                model = File(it),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.2f
            )
        }
        Text(
            day.toString(),
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.SemiBold
        )
        mood?.let {
            MoodIcon(
                mood = it,
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                size = 23.dp
            )
        }
    }
}

private fun weekdayLabel(value: Int): String =
    listOf("월", "화", "수", "목", "금", "토", "일")[value - 1]
