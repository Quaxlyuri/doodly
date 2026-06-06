package com.doodly.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: DiaryRepository,
    private val entryId: Int
) : ViewModel() {
    private val _entry = MutableStateFlow<DiaryEntry?>(null)
    val entry: StateFlow<DiaryEntry?> = _entry

    init {
        viewModelScope.launch { _entry.value = repository.findById(entryId) }
    }

    fun delete(onDeleted: () -> Unit) {
        val current = _entry.value ?: return
        viewModelScope.launch {
            repository.delete(current)
            onDeleted()
        }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val entryId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DetailViewModel(repository, entryId) as T
    }
}
