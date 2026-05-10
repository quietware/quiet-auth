package dev.calmauth.data

class PinRepository(private val storage: SecureStorage) {

    fun loadPinHash(): String? = storage.getItem(KEY_PIN_HASH)

    fun savePinHash(hash: String) = storage.setItem(KEY_PIN_HASH, hash)

    fun clearPin() {
        storage.deleteItem(KEY_PIN_HASH)
        storage.deleteItem(KEY_BIOMETRIC_PIN)
        storage.deleteItem(KEY_BIOMETRIC_ENABLED)
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
        const val KEY_PIN_HASH = "calmauth_pin_hash_v1"
        const val KEY_BIOMETRIC_PIN = "calmauth_biometric_pin_v1"
        const val KEY_BIOMETRIC_ENABLED = "calmauth_biometric_enabled_v1"
    }
}
