package io.quiet.auth.ui.screens

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import io.quiet.auth.R
import io.quiet.auth.domain.parseOtpAuthUri
import io.quiet.auth.ui.components.PageScaffold
import io.quiet.auth.ui.components.PrimaryButton
import io.quiet.auth.ui.components.SecondaryButton
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun AddTwoFAQrScreen(
    twoFAViewModel: TwoFAViewModel,
    onAddManually: () -> Unit,
    onCancel: () -> Unit,
    onAdded: () -> Unit,
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result.contents ?: return@rememberLauncherForActivityResult
        val parsed = parseOtpAuthUri(raw)
        if (parsed == null) {
            AlertDialog.Builder(context)
                .setTitle(R.string.invalidQrTitle)
                .setMessage(R.string.invalidQrMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return@rememberLauncherForActivityResult
        }
        twoFAViewModel.addParsedOtpAuth(parsed)
        AlertDialog.Builder(context)
            .setTitle(R.string.qrAddedTitle)
            .setMessage(R.string.qrAddedMessage)
            .setPositiveButton(android.R.string.ok) { _, _ -> onAdded() }
            .setOnDismissListener { onAdded() }
            .show()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) launchScan(scanLauncher, context)
    }

    LaunchedEffect(Unit) {
        if (permissionGranted) launchScan(scanLauncher, context)
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
            text = stringResource(R.string.qrTitle),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.qrSubtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))
        if (!permissionGranted) {
            Text(
                text = stringResource(R.string.qrPermissionRequired),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.enableCamera),
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
        } else {
            PrimaryButton(
                text = stringResource(R.string.qrTitle),
                onClick = { launchScan(scanLauncher, context) },
            )
        }

        Spacer(Modifier.weight(1f))
        SecondaryButton(text = stringResource(R.string.addManually), onClick = onAddManually)
        Spacer(Modifier.height(8.dp))
        SecondaryButton(text = stringResource(R.string.cancel), onClick = onCancel)
    }
}

private fun launchScan(
    launcher: androidx.activity.result.ActivityResultLauncher<ScanOptions>,
    context: android.content.Context,
) {
    val options = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt(context.getString(R.string.qrSubtitle))
        setBeepEnabled(false)
        setOrientationLocked(false)
    }
    launcher.launch(options)
}
