package io.quiet.auth.ui.nav

object Routes {
    const val START = "start"
    const val ONBOARDING = "onboarding"
    /** Pattern for [pin] destinations — use with [pin] helper for concrete routes. */
    const val PIN_ROUTE = "pin/{pinMode}"
    const val TWOFAS = "twofas"
    const val TOKEN_DETAILS = "token/{id}"
    const val ADD_TWOFA = "add-2fa"
    const val ADD_TWOFA_QR = "add-2fa-qr"
    const val SETTINGS = "settings"
    const val SETTINGS_SECURITY = "settings/security"
    const val SETTINGS_BACKUP = "settings/backup"
    const val SETTINGS_DANGER_ZONE = "settings/danger-zone"
    const val BACKUP_PROCESSING = "backup-processing/{action}"
    const val DEVELOPER_MODE = "developer-mode"

    fun pin(pinMode: String) = "pin/$pinMode"

    fun tokenDetails(id: String) = "token/$id"
    fun backupProcessing(action: String) = "backup-processing/$action"

    const val ARG_ID = "id"
    const val ARG_ACTION = "action"
    const val ARG_PIN_MODE = "pinMode"
}

object PinRouteMode {
    const val SETUP = "setup"
    const val UNLOCK = "unlock"
    const val VERIFY_DISABLE = "verify_disable"
}
