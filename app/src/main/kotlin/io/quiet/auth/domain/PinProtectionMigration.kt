package io.quiet.auth.domain

/**
 * Pure migration rule for optional PIN: explicit stored flag wins; legacy installs have no flag
 * but may have a PIN hash.
 */
object PinProtectionMigration {
    fun isProtectionEnabled(explicitFlag: String?, hasStoredPinHash: Boolean): Boolean = when (explicitFlag) {
        "0" -> false
        "1" -> true
        null -> hasStoredPinHash
        else -> false
    }
}
