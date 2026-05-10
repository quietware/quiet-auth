package io.quiet.auth.data

import io.quiet.auth.domain.PinProtectionMigration

class PinRepository(private val storage: SecureStorage) {

    fun loadPinHash(): String? = storage.getItem(KEY_PIN_HASH)

    fun savePinHash(hash: String) = storage.setItem(KEY_PIN_HASH, hash)

    /**
     * Whether PIN protection is enabled. Migrates legacy installs: no explicit flag but a stored
     * hash implies protection was always on.
     */
    fun loadPinProtectionEnabled(): Boolean {
        val explicit = storage.getItem(KEY_PIN_ENABLED)
        val hash = loadPinHash()
        val enabled = PinProtectionMigration.isProtectionEnabled(explicit, hash != null)
        if (explicit == null && hash != null) {
            storage.setItem(KEY_PIN_ENABLED, PIN_ENABLED)
        }
        return enabled
    }

    fun setPinProtectionEnabled(enabled: Boolean) {
        storage.setItem(KEY_PIN_ENABLED, if (enabled) PIN_ENABLED else PIN_DISABLED)
    }

    fun isOnboardingCompleted(): Boolean = storage.getItem(KEY_ONBOARDING_COMPLETED) == "1"

    fun setOnboardingCompleted() {
        storage.setItem(KEY_ONBOARDING_COMPLETED, "1")
    }

    fun clearOnboardingCompleted() {
        storage.deleteItem(KEY_ONBOARDING_COMPLETED)
    }

    fun clearPin() {
        storage.deleteItem(KEY_PIN_HASH)
        storage.deleteItem(KEY_BIOMETRIC_PIN)
        storage.deleteItem(KEY_BIOMETRIC_ENABLED)
        setPinProtectionEnabled(false)
    }

    fun loadBiometricPin(): String? = storage.getItem(KEY_BIOMETRIC_PIN)

    fun saveBiometricPin(pin: String) {
        storage.setItem(KEY_BIOMETRIC_PIN, pin)
        storage.setItem(KEY_BIOMETRIC_ENABLED, "1")
    }

    fun isBiometricEnabled(): Boolean = storage.getItem(KEY_BIOMETRIC_ENABLED) == "1"

    fun clearBiometric() {
        storage.deleteItem(KEY_BIOMETRIC_PIN)
        storage.deleteItem(KEY_BIOMETRIC_ENABLED)
    }

    companion object {
        const val KEY_PIN_HASH = "quietauth_pin_hash_v1"
        const val KEY_BIOMETRIC_PIN = "quietauth_biometric_pin_v1"
        const val KEY_BIOMETRIC_ENABLED = "quietauth_biometric_enabled_v1"
        const val KEY_PIN_ENABLED = "quietauth_pin_enabled_v1"
        const val KEY_ONBOARDING_COMPLETED = "quietauth_onboarding_completed_v1"

        private const val PIN_ENABLED = "1"
        private const val PIN_DISABLED = "0"
    }
}
