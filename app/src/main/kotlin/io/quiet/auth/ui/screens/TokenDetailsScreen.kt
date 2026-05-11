package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.quiet.auth.R
import io.quiet.auth.domain.formatLiveTotpCode
import io.quiet.auth.domain.secondsLeftForPeriod
import io.quiet.auth.ui.components.EmptyState
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.components.TokenCode
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun TokenDetailsScreen(
    tokenId: String,
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    onBack: () -> Unit,
    onLocked: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()
    val nowMs by useCurrentTime()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pin.isLoading, pin.isPinEnabled, pin.isUnlocked) {
        if (!pin.isLoading && pin.isPinEnabled && !pin.isUnlocked) onLocked()
    }

    val item by remember(tokenId, twoFA.items) {
        derivedStateOf { twoFA.items.firstOrNull { it.id == tokenId } }
    }
    QuietScaffold(
        title = item?.name ?: stringResource(R.string.appName),
        subtitle = item?.account ?: stringResource(R.string.tokenNotFound),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.backToTokens),
                onPrimaryClick = onBack,
                secondaryLabel = if (item != null) stringResource(R.string.confirmDelete) else null,
                onSecondaryClick = if (item != null) ({ showDeleteDialog = true }) else null,
            )
        },
    ) {
        if (pin.isLoading || (pin.isPinEnabled && !pin.isUnlocked)) {
            EmptyState(title = stringResource(R.string.unlockingApp), description = "")
            return@QuietScaffold
        }
        if (item == null) {
            EmptyState(title = stringResource(R.string.tokenNotFound), description = "")
            return@QuietScaffold
        }
        val current = item!!
        Spacer(Modifier.height(16.dp))
        TokenCode(
            code = formatLiveTotpCode(current, nowMs),
            expiresInSeconds = secondsLeftForPeriod(current.period, nowMs),
        )
    }

    if (showDeleteDialog && item != null) {
        ConfirmActionSheet(
            title = stringResource(R.string.deleteTokenTitle),
            message = stringResource(R.string.deleteTokenMessage),
            confirmLabel = stringResource(R.string.confirmDelete),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                twoFAViewModel.removeTwoFA(item!!.id)
                showDeleteDialog = false
                onBack()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
