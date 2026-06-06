package com.doodly.app.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.doodly.app.data.local.DiaryEntry
import com.doodly.app.data.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow

class GalleryViewModel(repository: DiaryRepository) : ViewModel() {
    val entries: Flow<List<DiaryEntry>> = repository.allEntries

    class Factory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GalleryViewModel(repository) as T
    }
}
