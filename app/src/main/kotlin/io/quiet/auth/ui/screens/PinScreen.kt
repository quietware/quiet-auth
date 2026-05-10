package io.quiet.auth.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.quiet.auth.R
import io.quiet.auth.ui.components.PageScaffold
import io.quiet.auth.ui.components.PrimaryButton
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

    PageScaffold {
        Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(36.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(PIN_LENGTH) { index ->
                    val filled = index < pin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape)
                            .background(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                    )
                }
            }
        }

        Keypad(
            onDigit = { digit -> if (pin.length < PIN_LENGTH) pin += digit },
            onBackspace = { pin = pin.dropLast(1) },
        )

        Spacer(Modifier.height(8.dp))
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
        PrimaryButton(
            text = stringResource(R.string.continueLabel),
            onClick = ::onContinue,
            enabled = pin.length >= PIN_LENGTH,
        )
    }
}

@Composable
private fun Keypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "<x"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                for (key in row) {
                    KeypadKey(
                        label = key,
                        onClick = {
                            when (key) {
                                "" -> Unit
                                "<x" -> onBackspace()
                                else -> onDigit(key)
                            }
                        },
                        isPlaceholder = key == "",
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    label: String,
    onClick: () -> Unit,
    isPlaceholder: Boolean,
) {
    val display = if (label == "<x") "\u232B" else label
    Box(
        modifier = Modifier
            .height(56.dp)
            .width(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .let { mod ->
                if (isPlaceholder) mod
                else mod
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onClick)
            },
        contentAlignment = Alignment.Center,
    ) {
        if (!isPlaceholder) {
            Text(
                text = display,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
