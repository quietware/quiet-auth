package dev.calmauth.ui.screens

import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import dev.calmauth.R
import dev.calmauth.domain.formatLiveTotpCode
import dev.calmauth.domain.secondsLeftForPeriod
import dev.calmauth.ui.components.PageScaffold
import dev.calmauth.ui.components.PrimaryButton
import dev.calmauth.ui.components.SecondaryButton
import dev.calmauth.ui.viewmodel.PinViewModel
import dev.calmauth.ui.viewmodel.TwoFAViewModel

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
    val context = LocalContext.current
    val nowMs by useCurrentTime()

    LaunchedEffect(pin.isLoading, pin.isUnlocked) {
        if (!pin.isLoading && !pin.isUnlocked) onLocked()
    }

    if (pin.isLoading || !pin.isUnlocked) {
        PageScaffold {
            Text(
                text = stringResource(R.string.unlockingApp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val item by remember(tokenId, twoFA.items) {
        derivedStateOf { twoFA.items.firstOrNull { it.id == tokenId } }
    }
    if (item == null) {
        PageScaffold {
            Text(
                text = stringResource(R.string.appName),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.tokenNotFound),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            SecondaryButton(text = stringResource(R.string.backToTokens), onClick = onBack)
        }
        return
    }

    val current = item!!

    PageScaffold {
        Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = current.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = current.account,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.tokenCode).uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = formatLiveTotpCode(current, nowMs),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${stringResource(R.string.codeExpiresIn)} ${secondsLeftForPeriod(current.period, nowMs)}s",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f))

        SecondaryButton(text = stringResource(R.string.backToTokens), onClick = onBack)
        Spacer(Modifier.height(8.dp))
        PrimaryButton(text = stringResource(R.string.confirmDelete), onClick = {
            AlertDialog.Builder(context)
                .setTitle(R.string.deleteTokenTitle)
                .setMessage(R.string.deleteTokenMessage)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirmDelete) { _, _ ->
                    twoFAViewModel.removeTwoFA(current.id)
                    onBack()
                }
                .show()
        })
    }
}
