package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.quiet.auth.R
import io.quiet.auth.domain.AddTwoFAInput
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
import io.quiet.auth.ui.components.TextFieldMMD
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun AddTwoFAScreen(
    twoFAViewModel: TwoFAViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    var serviceName by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var showMissing by remember { mutableStateOf(false) }

    fun save() {
        if (serviceName.isBlank() || account.isBlank() || secret.isBlank()) {
            showMissing = true
            return
        }
        twoFAViewModel.addTwoFA(
            AddTwoFAInput(
                name = serviceName,
                account = account,
                secret = secret,
            )
        )
        onSaved()
    }

    QuietScaffold(
        title = stringResource(R.string.add2faTitle),
        subtitle = stringResource(R.string.add2faSubtitle),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.save2fa),
                onPrimaryClick = ::save,
                secondaryLabel = stringResource(R.string.cancel),
                onSecondaryClick = onCancel,
            )
        },
    ) {
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TextFieldMMD(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = stringResource(R.string.serviceName),
                modifier = Modifier.fillMaxWidth(),
            )
            TextFieldMMD(
                value = account,
                onValueChange = { account = it },
                label = stringResource(R.string.account),
                modifier = Modifier.fillMaxWidth(),
            )
            TextFieldMMD(
                value = secret,
                onValueChange = { secret = it },
                label = stringResource(R.string.secretKey),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showMissing) {
        ConfirmActionSheet(
            title = stringResource(R.string.missingFieldsTitle),
            message = stringResource(R.string.missingFieldsMessage),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { showMissing = false },
            onDismiss = { showMissing = false },
        )
    }
}
