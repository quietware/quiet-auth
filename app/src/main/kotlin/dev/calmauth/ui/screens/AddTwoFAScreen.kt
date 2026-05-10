package dev.calmauth.ui.screens

import android.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import dev.calmauth.R
import dev.calmauth.domain.AddTwoFAInput
import dev.calmauth.ui.components.PageScaffold
import dev.calmauth.ui.components.PrimaryButton
import dev.calmauth.ui.components.SecondaryButton
import dev.calmauth.ui.viewmodel.TwoFAViewModel

@Composable
fun AddTwoFAScreen(
    twoFAViewModel: TwoFAViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    var serviceName by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }

    fun save() {
        if (serviceName.isBlank() || account.isBlank() || secret.isBlank()) {
            AlertDialog.Builder(context)
                .setTitle(R.string.missingFieldsTitle)
                .setMessage(R.string.missingFieldsMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show()
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

    PageScaffold {
        Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.add2faTitle),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.add2faSubtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text(stringResource(R.string.serviceName)) },
                placeholder = { Text(stringResource(R.string.servicePlaceholder)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text(stringResource(R.string.account)) },
                placeholder = { Text(stringResource(R.string.accountPlaceholder)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = secret,
                onValueChange = { secret = it },
                label = { Text(stringResource(R.string.secretKey)) },
                placeholder = { Text(stringResource(R.string.secretPlaceholder)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(text = stringResource(R.string.save2fa), onClick = ::save)
        Spacer(Modifier.height(8.dp))
        SecondaryButton(text = stringResource(R.string.cancel), onClick = onCancel)
    }
}
