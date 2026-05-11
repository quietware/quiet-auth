package io.quiet.auth.ui.screens

import android.app.AlertDialog
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.quiet.auth.R
import io.quiet.auth.data.PinRepository
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun DangerZoneScreen(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    pinRepository: PinRepository,
    onBack: () -> Unit,
    onAfterReset: () -> Unit,
    @StringRes backButtonLabelRes: Int,
) {
    val context = LocalContext.current

    fun confirm(
        titleRes: Int,
        messageRes: Int,
        successMessageRes: Int,
        action: () -> Boolean,
        navigateAfter: Boolean,
    ) {
        AlertDialog.Builder(context)
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.developerConfirmAction) { _, _ ->
                val ok = action()
                if (ok) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.developerActionSuccessTitle)
                        .setMessage(successMessageRes)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            if (navigateAfter) onAfterReset()
                        }
                        .setOnDismissListener { if (navigateAfter) onAfterReset() }
                        .show()
                } else {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.developerActionErrorTitle)
                        .setMessage(R.string.developerActionErrorMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            .show()
    }

    QuietScaffold(
        title = stringResource(R.string.dangerZoneTitle),
        subtitle = stringResource(R.string.dangerZoneSubtitle),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(backButtonLabelRes),
                onPrimaryClick = onBack,
            )
        },
    ) {
        Spacer(Modifier.height(20.dp))
        io.quiet.auth.ui.components.PrimaryButton(text = stringResource(R.string.developerResetStorage), onClick = {
            confirm(
                titleRes = R.string.developerResetStorageTitle,
                messageRes = R.string.developerResetStorageConfirmMessage,
                successMessageRes = R.string.developerResetStorageSuccessMessage,
                action = {
                    pinRepository.clearOnboardingCompleted()
                    val pinRemoved = pinViewModel.removePin()
                    twoFAViewModel.replaceAll(emptyList())
                    pinRemoved
                },
                navigateAfter = true,
            )
        })
        Spacer(Modifier.height(8.dp))
        io.quiet.auth.ui.components.PrimaryButton(text = stringResource(R.string.developerRemovePin), onClick = {
            confirm(
                titleRes = R.string.developerRemovePinTitle,
                messageRes = R.string.developerRemovePinConfirmMessage,
                successMessageRes = R.string.developerRemovePinSuccessMessage,
                action = {
                    pinRepository.clearOnboardingCompleted()
                    pinViewModel.removePin()
                },
                navigateAfter = true,
            )
        })
        Spacer(Modifier.height(8.dp))
        io.quiet.auth.ui.components.PrimaryButton(text = stringResource(R.string.developerRemoveBiometrics), onClick = {
            confirm(
                titleRes = R.string.developerRemoveBiometricsTitle,
                messageRes = R.string.developerRemoveBiometricsConfirmMessage,
                successMessageRes = R.string.developerRemoveBiometricsSuccessMessage,
                action = { pinViewModel.removeBiometrics() },
                navigateAfter = false,
            )
        })
    }
}
