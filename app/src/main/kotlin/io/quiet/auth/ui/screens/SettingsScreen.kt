package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.quiet.auth.R
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietListRow
import io.quiet.auth.ui.components.QuietScaffold

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSecurity: () -> Unit,
    onBackup: () -> Unit,
    onDangerZone: () -> Unit,
) {
    QuietScaffold(
        title = stringResource(R.string.settingsTitle),
        subtitle = stringResource(R.string.settingsHubSubtitle),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.settingsBack),
                onPrimaryClick = onBack,
            )
        },
    ) {
        Spacer(Modifier.height(10.dp))
        Column {
            QuietListRow(title = stringResource(R.string.settingsSecurityTitle), onClick = onSecurity)
            QuietListRow(title = stringResource(R.string.settingsBackupTitle), onClick = onBackup)
            QuietListRow(title = stringResource(R.string.dangerZoneTitle), onClick = onDangerZone)
        }
    }
}
