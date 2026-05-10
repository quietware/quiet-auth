package io.quiet.auth.data

import android.content.Context
import android.net.Uri
import io.quiet.auth.session.SessionLockController
import java.io.IOException

/**
 * Thin Android wrapper around the SAF callbacks produced by the UI layer. The pickFile /
 * createFile entry points yield a [Uri] selected by the user via the system picker; this
 * class then turns the URI into the same string contract that
 * [adapters/expo/backup-io-adapter.ts] exposed.
 *
 * Session-lock deferral is mirrored from [utils/session-lock-defer.ts]: while we are bouncing
 * to the system file picker, an AppState background should not lock the PIN session.
 */
class BackupIO(private val context: Context, private val sessionLock: SessionLockController) {

    fun beginInteractiveBackup() {
        sessionLock.beginDefer()
    }

    fun endInteractiveBackup() {
        sessionLock.endDefer()
    }

    fun writeCsv(uri: Uri, csv: String) {
        try {
            context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                output.write(csv.toByteArray(Charsets.UTF_8))
                output.flush()
            } ?: throw IOException("Could not open output stream for $uri")
        } finally {
            sessionLock.endDefer()
        }
    }

    fun readCsv(uri: Uri): String {
        try {
            return context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            } ?: throw IOException("Could not open input stream for $uri")
        } finally {
            sessionLock.endDefer()
        }
    }

    companion object {
        const val MIME_CSV = "text/csv"
        fun suggestedBackupFileName(): String = "quietauth-backup-${System.currentTimeMillis()}.csv"
    }
}
