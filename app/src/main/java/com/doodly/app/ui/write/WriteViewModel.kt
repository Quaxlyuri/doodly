package com.doodly.app.ui.write

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.local.Mood
import com.doodly.app.data.repository.AiRepository
import com.doodly.app.data.repository.BackupRepository
import com.doodly.app.data.repository.DiaryRepository
import com.doodly.app.util.normalizeDate
import com.doodly.app.util.todayMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WriteUiState(
    val date: Long = todayMillis(),
    val content: String = "",
    val mood: Mood = Mood.HAPPY,
    val imagePath: String? = null,
    val editingId: Int? = null,
    val originalCreatedAt: Long? = null,
    val isLoadingEntry: Boolean = false,
    val isGenerating: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedId: Int? = null
)

class WriteViewModel(
    private val diaryRepository: DiaryRepository,
    private val aiRepository: AiRepository,
    private val backupRepository: BackupRepository,
    initialDate: Long?
) : ViewModel() {
    private val initialSafeDate = safeDate(initialDate ?: todayMillis())
    private val _uiState = MutableStateFlow(WriteUiState(date = initialSafeDate))
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    init {
        loadDate(initialSafeDate)
    }

    fun setDate(value: Long) {
        loadDate(safeDate(value))
    }

    fun setContent(value: String) = _uiState.update {
        it.copy(content = value, imagePath = null, error = null, savedId = null)
    }

    fun setMood(value: Mood) = _uiState.update {
        it.copy(mood = value, imagePath = null, savedId = null)
    }

    fun generateImage() {
        val state = _uiState.value
        if (state.content.isBlank() || state.isGenerating || state.isLoadingEntry) return
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            runCatching {
                aiRepository.generateDiaryImage(state.content.trim(), state.mood)
            }.onSuccess { path ->
                _uiState.update { it.copy(imagePath = path, isGenerating = false) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = throwable.message ?: "그림을 만들지 못했어요."
                    )
                }
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val imagePath = state.imagePath ?: return
        if (state.isSaving || state.isLoadingEntry) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                val entry = DiaryEntry(
                    id = state.editingId ?: 0,
                    date = safeDate(state.date),
                    content = state.content.trim(),
                    mood = state.mood.name,
                    imagePath = imagePath,
                    tags = listOf(state.mood.label),
                    createdAt = state.originalCreatedAt ?: System.currentTimeMillis()
                )
                val id = if (state.editingId != null) {
                    diaryRepository.update(entry)
                    state.editingId
                } else {
                    diaryRepository.insert(entry)
                }
                backupRepository.backup(entry.copy(id = id))
                id
            }.onSuccess { id ->
                _uiState.update { it.copy(isSaving = false, savedId = id) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(isSaving = false, error = throwable.message ?: "저장하지 못했어요.")
                }
            }
        }
    }

    private fun loadDate(date: Long) {
        viewModelScope.launch {
            _uiState.value = WriteUiState(date = date, isLoadingEntry = true)
            val existing = diaryRepository.findByDate(date)
            _uiState.value = if (existing == null) {
                WriteUiState(date = date)
            } else {
                WriteUiState(
                    date = date,
                    content = existing.content,
                    mood = Mood.fromName(existing.mood),
                    imagePath = existing.imagePath,
                    editingId = existing.id,
                    originalCreatedAt = existing.createdAt
                )
            }
        }
    }

    private fun safeDate(value: Long): Long =
        normalizeDate(value).coerceAtMost(todayMillis())

    class Factory(
        private val diaryRepository: DiaryRepository,
        private val aiRepository: AiRepository,
        private val backupRepository: BackupRepository,
        private val initialDate: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WriteViewModel(
                diaryRepository,
                aiRepository,
                backupRepository,
                initialDate
            ) as T
        }
    }
}
