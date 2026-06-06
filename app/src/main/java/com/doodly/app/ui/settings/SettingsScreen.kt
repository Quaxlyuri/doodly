package com.doodly.app.ui.settings

import android.Manifest
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.doodly.app.ui.component.TossCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "설정",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
        )
        TossCard(modifier = Modifier.fillMaxWidth()) {
            SettingRow(
                title = "매일 일기 알림",
                description = "정한 시간에 오늘의 기록을 알려드려요.",
                checked = state.reminderEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) notificationPermission?.launchPermissionRequest()
                    viewModel.setReminder(context, enabled)
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("알림 시각")
                Text(
                    "%02d:%02d".format(state.hour, state.minute),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        TossCard(modifier = Modifier.fillMaxWidth()) {
            SettingRow(
                title = "클라우드 백업",
                description = "Firebase 옵션 모듈을 활성화하면 사용할 수 있어요.",
                checked = state.cloudBackupEnabled,
                onCheckedChange = viewModel::setCloudBackup
            )
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(
            initialHour = state.hour,
            initialMinute = state.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("알림 시각") },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setTime(context, pickerState.hour, pickerState.minute)
                        showTimePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
