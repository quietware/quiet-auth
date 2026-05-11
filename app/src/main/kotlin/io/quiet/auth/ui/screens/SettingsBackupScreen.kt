package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.quiet.auth.R
import io.quiet.auth.ui.components.BackupInfo
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.sessionReadyForSensitiveActions
import io.quiet.auth.ui.patterns.RestoreFlow

@Composable
fun SettingsBackupScreen(
    pinViewModel: PinViewModel,
    onBack: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    var showSessionDialog by remember { mutableStateOf(false) }

    fun ensureSessionReady(titleRes: Int): Boolean {
        if (!pin.sessionReadyForSensitiveActions) {
            showSessionDialog = true
            return false
        }
        return true
    }

    QuietScaffold(
        title = stringResource(R.string.settingsBackupTitle),
        subtitle = stringResource(R.string.settingsBackupSubtitle),
        bottomBar = { io.quiet.auth.ui.components.QuietBottomActions(
            primaryLabel = stringResource(R.string.settingsBack),
            onPrimaryClick = onBack,
        ) },
    ) {
        Spacer(Modifier.height(12.dp))
        RestoreFlow(
            createBackupLabel = stringResource(R.string.createBackup),
            restoreBackupLabel = stringResource(R.string.restoreBackup),
            onCreateBackup = { if (ensureSessionReady(R.string.backupErrorTitle)) onCreateBackup() },
            onRestoreBackup = { if (ensureSessionReady(R.string.restoreErrorTitle)) onRestoreBackup() },
        )
        Spacer(Modifier.height(12.dp))
        BackupInfo(text = stringResource(R.string.backupProcessingHint))
    }

    if (showSessionDialog) {
        ConfirmActionSheet(
            title = stringResource(R.string.backupErrorTitle),
            message = stringResource(R.string.sessionExpiredMessage),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { showSessionDialog = false },
            onDismiss = { showSessionDialog = false },
        )
    }
}
