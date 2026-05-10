package dev.calmauth.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import dev.calmauth.R
import dev.calmauth.data.BackupIO
import dev.calmauth.domain.BackupFormatException
import dev.calmauth.domain.parseBackupCsv
import dev.calmauth.domain.twoFAItemsToCsv
import dev.calmauth.ui.viewmodel.PinViewModel
import dev.calmauth.ui.viewmodel.TwoFAViewModel
import dev.calmauth.ui.viewmodel.sessionReadyForSensitiveActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BackupProcessingScreen(
    action: String,
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    backupIO: BackupIO,
    onFinished: () -> Unit,
    onLocked: () -> Unit,
) {
    val isRestore = action == "restore"
    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember {
        mutableStateOf(
            context.getString(
                if (isRestore) R.string.restoringBackupProgress else R.string.creatingBackupProgress
            )
        )
    }

    val createLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupIO.MIME_CSV)
    ) { uri ->
        scope.launch {
            try {
                if (uri == null) {
                    status = context.getString(R.string.backupErrorMessage)
                } else {
                    backupIO.writeCsv(uri, twoFAItemsToCsv(twoFA.items))
                    status = context.getString(R.string.backupCreatedTitle)
                }
            } catch (_: Throwable) {
                status = context.getString(R.string.backupErrorMessage)
            } finally {
                delay(1100)
                onFinished()
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        scope.launch {
            try {
                if (uri == null) {
                    status = context.getString(R.string.restoreErrorMessage)
                } else {
                    val csv = backupIO.readCsv(uri)
                    val items = parseBackupCsv(csv)
                    twoFAViewModel.replaceAll(items)
                    status = context.getString(R.string.restoreSuccessTitle)
                }
            } catch (_: BackupFormatException) {
                status = context.getString(R.string.restoreErrorMessage)
            } catch (_: Throwable) {
                status = context.getString(R.string.restoreErrorMessage)
            } finally {
                delay(1100)
                onFinished()
            }
        }
    }

    LaunchedEffect(action, pin.sessionReadyForSensitiveActions) {
        if (!pin.sessionReadyForSensitiveActions) {
            status = context.getString(R.string.sessionExpiredMessage)
            delay(900)
            onLocked()
            return@LaunchedEffect
        }
        backupIO.beginInteractiveBackup()
        if (isRestore) {
            openLauncher.launch(arrayOf(BackupIO.MIME_CSV, "text/comma-separated-values", "text/plain"))
        } else {
            createLauncher.launch(BackupIO.suggestedBackupFileName())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(if (isRestore) R.string.restoreBackup else R.string.createBackup),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
