package com.doodly.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.doodly.app.data.repository.SettingsRepository
import com.doodly.app.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val reminderEnabled: Boolean,
    val hour: Int,
    val minute: Int,
    val cloudBackupEnabled: Boolean
)

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            reminderEnabled = repository.reminderEnabled,
            hour = repository.reminderHour,
            minute = repository.reminderMinute,
            cloudBackupEnabled = repository.cloudBackupEnabled
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun setReminder(context: Context, enabled: Boolean) {
        repository.reminderEnabled = enabled
        _uiState.update { it.copy(reminderEnabled = enabled) }
        if (enabled) {
            ReminderScheduler.schedule(context, _uiState.value.hour, _uiState.value.minute)
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    fun setTime(context: Context, hour: Int, minute: Int) {
        repository.reminderHour = hour
        repository.reminderMinute = minute
        _uiState.update { it.copy(hour = hour, minute = minute) }
        if (_uiState.value.reminderEnabled) {
            ReminderScheduler.schedule(context, hour, minute)
        }
    }

    fun setCloudBackup(enabled: Boolean) {
        repository.cloudBackupEnabled = enabled
        _uiState.update { it.copy(cloudBackupEnabled = enabled) }
    }

    class Factory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(repository) as T
    }
}
