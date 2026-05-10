package dev.calmauth.ui.nav

object Routes {
    const val ONBOARDING = "onboarding"
    const val PIN = "pin"
    const val TWOFAS = "twofas"
    const val TOKEN_DETAILS = "token/{id}"
    const val ADD_TWOFA = "add-2fa"
    const val ADD_TWOFA_QR = "add-2fa-qr"
    const val SETTINGS = "settings"
    const val BACKUP_PROCESSING = "backup-processing/{action}"
    const val DEVELOPER_MODE = "developer-mode"

    fun tokenDetails(id: String) = "token/$id"
    fun backupProcessing(action: String) = "backup-processing/$action"

    const val ARG_ID = "id"
    const val ARG_ACTION = "action"
}
