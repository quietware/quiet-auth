package io.quiet.auth.ui.screens

import androidx.compose.runtime.Composable
import io.quiet.auth.R
import io.quiet.auth.data.PinRepository
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
    DangerZoneScreen(
        pinViewModel = pinViewModel,
        twoFAViewModel = twoFAViewModel,
        pinRepository = pinRepository,
        onBack = onBack,
        onAfterReset = onAfterReset,
        backButtonLabelRes = R.string.backToTokens,
    )
}
