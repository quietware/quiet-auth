package io.quiet.auth.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.quiet.auth.R
import io.quiet.auth.ui.components.PINPad
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.components.SecondaryButton
import io.quiet.auth.ui.nav.PinRouteMode
import io.quiet.auth.ui.viewmodel.PinViewModel
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    mode: String,
    onFinished: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var biometricAttempted by remember { mutableStateOf(false) }
    var biometricBusy by remember { mutableStateOf(false) }

    val setupLike = mode == PinRouteMode.SETUP
    val verifyDisable = mode == PinRouteMode.VERIFY_DISABLE
    val unlockLike = mode == PinRouteMode.UNLOCK || verifyDisable

    LaunchedEffect(state.hasPin, state.isPinEnabled, state.isUnlocked, state.isLoading, mode) {
        val shouldAutoNavigate =
            !state.isLoading &&
                state.isPinEnabled &&
                state.hasPin &&
                state.isUnlocked &&
                !verifyDisable
        if (shouldAutoNavigate) onFinished()
    }

    LaunchedEffect(
        mode,
        state.hasPin,
        state.hasBiometricUnlock,
        state.isPinEnabled,
        state.isUnlocked,
        state.isLoading,
    ) {
        if (
            mode != PinRouteMode.UNLOCK ||
            state.isLoading ||
            !state.isPinEnabled ||
            !state.hasPin ||
            !state.hasBiometricUnlock ||
            state.isUnlocked ||
            biometricAttempted
        ) {
            return@LaunchedEffect
        }
        biometricAttempted = true
        biometricBusy = true
        val ok = viewModel.unlockWithBiometrics(
            title = context.getString(R.string.pinUnlockTitle),
            cancelLabel = context.getString(R.string.cancel),
        )
        biometricBusy = false
        if (!ok) {
            Toast.makeText(
                context,
                context.getString(R.string.biometricUnlockFailedMessage),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun toast(titleRes: Int, msgRes: Int) {
        Toast.makeText(
            context,
            "${context.getString(titleRes)}: ${context.getString(msgRes)}",
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun onContinue() {
        val trimmed = pin.trim()
        if (trimmed.length < PIN_LENGTH) {
            toast(R.string.pinInvalidTitle, R.string.pinInvalidMessage)
            return
        }
        when {
            verifyDisable -> {
                if (!viewModel.disablePinProtectionAfterVerification(trimmed)) {
                    toast(R.string.pinIncorrectTitle, R.string.pinIncorrectMessage)
                    pin = ""
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.pinProtectionDisabledMessage),
                        Toast.LENGTH_SHORT,
                    ).show()
                    onFinished()
                }
            }
            setupLike -> {
                if (firstPin == null) {
                    if (!viewModel.validatePinFormat(trimmed)) {
                        toast(R.string.pinInvalidTitle, R.string.pinInvalidMessage)
                        return
                    }
                    firstPin = trimmed
                    pin = ""
                    return
                }
                if (trimmed != firstPin) {
                    toast(R.string.pinMismatchTitle, R.string.pinMismatchMessage)
                    firstPin = null
                    pin = ""
                    return
                }
                if (!viewModel.setPin(trimmed)) {
                    toast(R.string.pinStorageErrorTitle, R.string.pinStorageErrorMessage)
                    return
                }
            }
            unlockLike && state.hasPin -> {
                if (!viewModel.unlockWithPin(trimmed)) {
                    toast(R.string.pinIncorrectTitle, R.string.pinIncorrectMessage)
                    pin = ""
                }
            }
            else -> Unit
        }
    }

    val subtitle = when {
        verifyDisable -> stringResource(R.string.pinVerifyDisableSubtitle)
        state.hasPin && unlockLike -> stringResource(R.string.pinUnlockSubtitle)
        setupLike && firstPin != null -> stringResource(R.string.pinConfirmPlaceholder)
        setupLike -> stringResource(R.string.pinSetupSubtitleOptional)
        else -> stringResource(R.string.pinUnlockSubtitle)
    }

    QuietScaffold(
        title = stringResource(R.string.pinUnlockTitle),
        subtitle = subtitle,
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.continueLabel),
                onPrimaryClick = ::onContinue,
                primaryEnabled = pin.length >= PIN_LENGTH,
            )
        },
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.pinCodePlaceholder),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            PINPad(
                pinLength = pin.length,
                onDigit = { digit -> if (pin.length < PIN_LENGTH) pin += digit },
                onBackspace = { pin = pin.dropLast(1) },
            )
        }

        if (
            mode == PinRouteMode.UNLOCK &&
            state.isPinEnabled &&
            state.hasPin &&
            state.isBiometricSupported
        ) {
            SecondaryButton(
                text = if (biometricBusy) stringResource(R.string.unlockingApp) else stringResource(R.string.useBiometrics),
                onClick = {
                    if (!biometricBusy) {
                        biometricAttempted = true
                        biometricBusy = true
                        scope.launch {
                            val ok = viewModel.unlockWithBiometrics(
                                title = context.getString(R.string.pinUnlockTitle),
                                cancelLabel = context.getString(R.string.cancel),
                            )
                            biometricBusy = false
                            if (!ok) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.biometricUnlockFailedMessage),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    }
                },
                enabled = !biometricBusy,
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
