package io.quiet.auth.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import io.quiet.auth.R
import io.quiet.auth.domain.parseOtpAuthUri
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.components.PrimaryButton
import io.quiet.auth.ui.components.QuietBottomActions
import io.quiet.auth.ui.components.QuietScaffold
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
    var dialogMessage by remember { mutableStateOf<Int?>(null) }
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
            dialogMessage = R.string.invalidQrMessage
            return@rememberLauncherForActivityResult
        }
        twoFAViewModel.addParsedOtpAuth(parsed)
        dialogMessage = R.string.qrAddedMessage
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

    QuietScaffold(
        title = stringResource(R.string.qrTitle),
        subtitle = stringResource(R.string.qrSubtitle),
        bottomBar = {
            QuietBottomActions(
                primaryLabel = stringResource(R.string.addManually),
                onPrimaryClick = onAddManually,
                secondaryLabel = stringResource(R.string.cancel),
                onSecondaryClick = onCancel,
            )
        },
    ) {

        Spacer(Modifier.height(24.dp))
        if (!permissionGranted) {
            Text(
                text = stringResource(R.string.qrPermissionRequired),
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

    }

    if (dialogMessage != null) {
        ConfirmActionSheet(
            title = stringResource(if (dialogMessage == R.string.qrAddedMessage) R.string.qrAddedTitle else R.string.invalidQrTitle),
            message = stringResource(dialogMessage!!),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                val msg = dialogMessage
                dialogMessage = null
                if (msg == R.string.qrAddedMessage) onAdded()
            },
            onDismiss = {
                val msg = dialogMessage
                dialogMessage = null
                if (msg == R.string.qrAddedMessage) onAdded()
            },
        )
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
