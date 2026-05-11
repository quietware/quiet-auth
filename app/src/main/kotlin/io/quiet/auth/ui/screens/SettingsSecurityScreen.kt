package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.quiet.auth.R
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.sessionReadyForSensitiveActions
import io.quiet.auth.ui.patterns.SecurityFlow
import kotlinx.coroutines.launch

@Composable
fun SettingsSecurityScreen(
    pinViewModel: PinViewModel,
    onBack: () -> Unit,
    onEnablePinProtection: () -> Unit,
    onDisablePinProtection: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var dialogMessage by remember { mutableStateOf<Int?>(null) }

    fun ensureSessionReady(): Boolean {
        if (!pin.sessionReadyForSensitiveActions) {
            dialogMessage = R.string.sessionExpiredMessage
            return false
        }
        return true
    }

    QuietScaffold(
        title = stringResource(R.string.settingsSecurityTitle),
        subtitle = stringResource(R.string.settingsSecuritySubtitle),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.settingsBack),
                onPrimaryClick = onBack,
            )
        },
    ) {
        Spacer(Modifier.height(10.dp))
        SecurityFlow(
            pinEnabled = pin.isPinEnabled,
            biometricsEnabled = pin.hasBiometricUnlock,
            biometricsAvailable = pin.isBiometricSupported && pin.sessionReadyForSensitiveActions,
            pinTitle = stringResource(R.string.pinProtectionSettingTitle),
            pinDescription = stringResource(R.string.pinProtectionSettingDescription),
            biometricsTitle = stringResource(R.string.biometricsSettingTitle),
            biometricsDescription = if (pin.isBiometricSupported) {
                stringResource(R.string.biometricsSettingDescription)
            } else {
                stringResource(R.string.biometricsUnavailableMessage)
            },
            onPinChange = { next ->
                if (next) onEnablePinProtection() else if (pin.isPinEnabled) onDisablePinProtection()
            },
            onBiometricsChange = { next ->
                if (ensureSessionReady()) {
                    scope.launch {
                        val ok = pinViewModel.setBiometricUnlockEnabled(
                            next,
                            title = "Biometric unlock",
                            cancelLabel = "Cancel",
                        )
                        if (!ok) dialogMessage = R.string.biometricSettingsErrorMessage
                    }
                }
            },
        )
    }

    if (dialogMessage != null) {
        ConfirmActionSheet(
            title = stringResource(R.string.biometricSettingsErrorTitle),
            message = stringResource(dialogMessage!!),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { dialogMessage = null },
            onDismiss = { dialogMessage = null },
        )
    }
}
