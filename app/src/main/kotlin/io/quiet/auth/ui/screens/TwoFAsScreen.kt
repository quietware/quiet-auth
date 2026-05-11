package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.quiet.auth.R
import io.quiet.auth.domain.formatLiveTotpCode
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.patterns.TokenListItemUi
import io.quiet.auth.ui.patterns.TokenListScreen
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun TwoFAsScreen(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    onTokenClick: (String) -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit,
    onLocked: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()
    val nowMs by useCurrentTime()

    LaunchedEffect(pin.isLoading, pin.isPinEnabled, pin.isUnlocked) {
        if (!pin.isLoading && pin.isPinEnabled && !pin.isUnlocked) onLocked()
    }

    QuietScaffold(
        title = stringResource(R.string.appName),
        subtitle = if (pin.isLoading || (pin.isPinEnabled && !pin.isUnlocked)) {
            stringResource(R.string.unlockingApp)
        } else {
            stringResource(R.string.twofaSubtitle)
        },
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.add2fa),
                onPrimaryClick = onAdd,
                secondaryLabel = stringResource(R.string.settingsCta),
                onSecondaryClick = onSettings,
            )
        },
    ) {
        Spacer(Modifier.height(6.dp))
        val uiItems = if (pin.isLoading || (pin.isPinEnabled && !pin.isUnlocked) || twoFA.isLoading) {
            emptyList()
        } else {
            twoFA.items.map {
                TokenListItemUi(
                    id = it.id,
                    name = it.name,
                    subtitle = it.account,
                    code = formatLiveTotpCode(it, nowMs),
                )
            }
        }
        val emptyMessage = when {
            pin.isLoading || (pin.isPinEnabled && !pin.isUnlocked) -> stringResource(R.string.unlockingApp)
            twoFA.isLoading -> stringResource(R.string.loadingAccounts)
            else -> stringResource(R.string.emptyAccounts)
        }
        TokenListScreen(
            items = uiItems,
            emptyMessage = emptyMessage,
            onTokenClick = onTokenClick,
        )
    }
}
