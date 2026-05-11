package io.quiet.auth.ui.patterns

import androidx.compose.runtime.Composable
import io.quiet.auth.ui.components.QuietListRow
import io.quiet.auth.ui.components.SwitchMMD

@Composable
fun SecurityFlow(
    pinEnabled: Boolean,
    biometricsEnabled: Boolean,
    biometricsAvailable: Boolean,
    pinTitle: String,
    pinDescription: String,
    biometricsTitle: String,
    biometricsDescription: String,
    onPinChange: (Boolean) -> Unit,
    onBiometricsChange: (Boolean) -> Unit,
) {
    QuietListRow(
        title = pinTitle,
        subtitle = pinDescription,
        trailing = {
            SwitchMMD(checked = pinEnabled, onCheckedChange = onPinChange)
        },
    )
    if (pinEnabled) {
        QuietListRow(
            title = biometricsTitle,
            subtitle = biometricsDescription,
            trailing = {
                SwitchMMD(
                    checked = biometricsEnabled,
                    onCheckedChange = onBiometricsChange,
                    enabled = biometricsAvailable,
                )
            },
        )
    }
}
