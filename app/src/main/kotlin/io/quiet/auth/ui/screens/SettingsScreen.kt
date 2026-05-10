package io.quiet.auth.ui.screens

import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.quiet.auth.R
import io.quiet.auth.ui.components.PageScaffold
import io.quiet.auth.ui.components.PrimaryButton
import io.quiet.auth.ui.components.SecondaryButton
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.sessionReadyForSensitiveActions
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    pinViewModel: PinViewModel,
    onBack: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onEnablePinProtection: () -> Unit,
    onDisablePinProtection: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun ensureSessionReady(titleRes: Int): Boolean {
        if (!pin.sessionReadyForSensitiveActions) {
            AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(R.string.sessionExpiredMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return false
        }
        return true
    }

    PageScaffold {
        Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.settingsTitle),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settingsSubtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pinProtectionSettingTitle),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.pinProtectionSettingDescription),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = pin.isPinEnabled,
                onCheckedChange = { next ->
                    if (next) {
                        onEnablePinProtection()
                    } else if (pin.isPinEnabled) {
                        onDisablePinProtection()
                    }
                },
            )
        }

        if (pin.isPinEnabled) {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.biometricsSettingTitle),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (pin.isBiometricSupported)
                            stringResource(R.string.biometricsSettingDescription)
                        else
                            stringResource(R.string.biometricsUnavailableMessage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = pin.hasBiometricUnlock,
                    onCheckedChange = { next ->
                        if (ensureSessionReady(R.string.biometricSettingsErrorTitle)) {
                            scope.launch {
                                val ok = pinViewModel.setBiometricUnlockEnabled(
                                    next,
                                    title = context.getString(R.string.biometricsSettingTitle),
                                    cancelLabel = context.getString(R.string.cancel),
                                )
                                if (!ok) {
                                    AlertDialog.Builder(context)
                                        .setTitle(R.string.biometricSettingsErrorTitle)
                                        .setMessage(R.string.biometricSettingsErrorMessage)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show()
                                }
                            }
                        }
                    },
                    enabled = pin.isBiometricSupported && pin.sessionReadyForSensitiveActions,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = stringResource(R.string.createBackup), onClick = {
            if (ensureSessionReady(R.string.backupErrorTitle)) onCreateBackup()
        })
        Spacer(Modifier.height(8.dp))
        SecondaryButton(text = stringResource(R.string.restoreBackup), onClick = {
            if (ensureSessionReady(R.string.restoreErrorTitle)) onRestoreBackup()
        })

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.backupProcessingHint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.weight(1f))
        SecondaryButton(text = stringResource(R.string.settingsBack), onClick = onBack)
    }
}
