package io.quiet.auth.ui.patterns

import androidx.compose.runtime.Composable
import io.quiet.auth.ui.components.QuietListRow

@Composable
fun RestoreFlow(
    createBackupLabel: String,
    restoreBackupLabel: String,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    QuietListRow(title = createBackupLabel, onClick = onCreateBackup)
    QuietListRow(title = restoreBackupLabel, onClick = onRestoreBackup)
}
