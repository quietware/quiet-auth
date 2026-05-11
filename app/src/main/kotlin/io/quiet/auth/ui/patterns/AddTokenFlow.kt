package io.quiet.auth.ui.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.quiet.auth.ui.components.HorizontalDividerMMD
import io.quiet.auth.ui.components.QuietListRow

@Composable
fun AddTokenFlow(
    onScanQr: () -> Unit,
    onManualEntry: () -> Unit,
    scanLabel: String,
    manualLabel: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        QuietListRow(title = scanLabel, onClick = onScanQr)
        HorizontalDividerMMD()
        QuietListRow(title = manualLabel, onClick = onManualEntry)
    }
}
