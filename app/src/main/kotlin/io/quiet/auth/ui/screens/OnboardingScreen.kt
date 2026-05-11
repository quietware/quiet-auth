package io.quiet.auth.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import io.quiet.auth.R
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold

@Composable
fun OnboardingScreen(
    onContinueWithoutPin: () -> Unit,
    onProtectWithPin: () -> Unit,
    onDeveloperMode: () -> Unit,
) {
    val tapCounter = remember { intArrayOf(0) }

    QuietScaffold(
        title = stringResource(R.string.appName),
        subtitle = stringResource(R.string.onboardingSubtitle),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.continueWithoutPin),
                onPrimaryClick = onContinueWithoutPin,
                secondaryLabel = stringResource(R.string.protectWithPin),
                onSecondaryClick = onProtectWithPin,
            )
        },
    ) {
        Text(
            text = stringResource(R.string.onboardingTitle),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            tapCounter[0] += 1
                            if (tapCounter[0] >= 5) {
                                tapCounter[0] = 0
                                onDeveloperMode()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "\uD83C\uDF3F", style = MaterialTheme.typography.displayLarge)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.onboardingPinHint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

    }
}
