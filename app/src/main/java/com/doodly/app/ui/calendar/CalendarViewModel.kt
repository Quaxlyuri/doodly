package com.doodly.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.local.Mood
import com.doodly.app.data.repository.AiRepository
import com.doodly.app.data.repository.DiaryRepository
import com.doodly.app.util.localDate
import com.doodly.app.util.monthRange
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class DailyMood(
    val date: LocalDate,
    val mood: Mood?
)

data class MoodInsightUiState(
    val days: List<DailyMood> = recentDates().map { DailyMood(it, null) },
    val recordCount: Int = 0,
    val dominantMood: Mood? = null,
    val message: String = "며칠의 마음을 기록하면 감정 흐름을 알려드릴게요.",
    val isAiLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val repository: DiaryRepository,
    private val aiRepository: AiRepository
) : ViewModel() {
    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month
    val entries: Flow<List<DiaryEntry>> = _month.flatMapLatest {
        val range = monthRange(it)
        repository.getMonth(range.first, range.last)
    }

    private val _moodInsight = MutableStateFlow(MoodInsightUiState())
    val moodInsight: StateFlow<MoodInsightUiState> = _moodInsight

    init {
        viewModelScope.launch {
            repository.allEntries.collectLatest(::updateMoodInsight)
        }
    }

    fun previousMonth() {
        _month.value = _month.value.minusMonths(1)
    }

    fun nextMonth() {
        if (_month.value < YearMonth.now()) {
            _month.value = _month.value.plusMonths(1)
        }
    }

    private suspend fun updateMoodInsight(allEntries: List<DiaryEntry>) {
        val dates = recentDates()
        val dateSet = dates.toSet()
        val recent = allEntries
            .filter { localDate(it.date) in dateSet }
            .distinctBy { localDate(it.date) }
            .sortedBy { it.date }
        val byDate = recent.associateBy { localDate(it.date) }
        val days = dates.map { date ->
            DailyMood(date, byDate[date]?.let { Mood.fromName(it.mood) })
        }
        val dominant = recent
            .groupingBy { Mood.fromName(it.mood) }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        val fallback = localInsight(recent)

        _moodInsight.value = MoodInsightUiState(
            days = days,
            recordCount = recent.size,
            dominantMood = dominant,
            message = fallback,
            isAiLoading = recent.size >= 2
        )
        if (recent.size >= 2) {
            val aiMessage = aiRepository.generateMoodInsight(recent, fallback)
            _moodInsight.value = _moodInsight.value.copy(
                message = aiMessage,
                isAiLoading = false
            )
        }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val aiRepository: AiRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CalendarViewModel(repository, aiRepository) as T
    }
}

private fun recentDates(): List<LocalDate> {
    val today = LocalDate.now()
    return (6 downTo 0).map { today.minusDays(it.toLong()) }
}

private fun localInsight(entries: List<DiaryEntry>): String {
    if (entries.isEmpty()) {
        return "오늘의 마음부터 천천히 남겨보세요. 변화는 기록에서 시작돼요."
    }
    if (entries.size == 1) {
        return "첫 마음을 잘 남겼어요. 두 번째 기록부터 흐름이 보이기 시작해요."
    }
    val moods = entries.map { Mood.fromName(it.mood) }
    val scores = moods.map(::moodScore)
    val split = scores.size / 2
    val earlier = scores.take(split.coerceAtLeast(1)).average()
    val recent = scores.drop(split.coerceAtLeast(1)).average()
    val dominant = moods.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

    return when {
        recent - earlier >= 0.8 ->
            "최근으로 올수록 마음이 한결 가벼워지고 있어요. 이 흐름을 기억해요."
        earlier - recent >= 0.8 ->
            "최근 마음이 조금 지쳐 보여요. 오늘은 나를 먼저 챙겨도 괜찮아요."
        dominant == Mood.TIRED ->
            "피곤한 날이 이어졌어요. 작은 쉼 하나를 오늘 일정에 넣어보세요."
        dominant == Mood.HAPPY || dominant == Mood.EXCITED ->
            "밝은 마음이 자주 찾아왔어요. 좋았던 순간을 오래 간직해봐요."
        dominant == Mood.CALM ->
            "잔잔하고 안정적인 흐름이에요. 지금의 편안함을 천천히 누려봐요."
        else ->
            "여러 감정이 자연스럽게 오갔어요. 어느 마음도 틀리지 않아요."
    }
}

private fun moodScore(mood: Mood): Int = when (mood) {
    Mood.HAPPY, Mood.EXCITED -> 2
    Mood.CALM -> 1
    Mood.TIRED -> -1
    Mood.SAD, Mood.ANGRY -> -2
}
