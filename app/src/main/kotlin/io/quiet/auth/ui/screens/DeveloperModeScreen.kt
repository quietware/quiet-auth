package io.quiet.auth.ui.screens

import android.app.AlertDialog
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
import io.quiet.auth.ui.components.PageScaffold
import io.quiet.auth.ui.components.PrimaryButton
import io.quiet.auth.ui.components.SecondaryButton
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun DeveloperModeScreen(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    pinRepository: PinRepository,
    onBack: () -> Unit,
    onAfterReset: () -> Unit,
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

    PageScaffold {
        Text(
            text = stringResource(R.string.developerModeTitle),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.developerModeSubtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(40.dp))
        PrimaryButton(text = stringResource(R.string.developerResetStorage), onClick = {
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
        PrimaryButton(text = stringResource(R.string.developerRemovePin), onClick = {
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
        PrimaryButton(text = stringResource(R.string.developerRemoveBiometrics), onClick = {
            confirm(
                titleRes = R.string.developerRemoveBiometricsTitle,
                messageRes = R.string.developerRemoveBiometricsConfirmMessage,
                successMessageRes = R.string.developerRemoveBiometricsSuccessMessage,
                action = { pinViewModel.removeBiometrics() },
                navigateAfter = false,
            )
        })

        Spacer(Modifier.weight(1f))
        SecondaryButton(text = stringResource(R.string.backToTokens), onClick = onBack)
    }
}
