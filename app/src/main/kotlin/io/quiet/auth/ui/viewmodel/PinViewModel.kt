package io.quiet.auth.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.quiet.auth.QuietAuthApp
import io.quiet.auth.auth.BiometricAuth
import io.quiet.auth.auth.BiometricAvailability
import io.quiet.auth.auth.BiometricResult
import io.quiet.auth.data.PinRepository
import io.quiet.auth.domain.PIN_REGEX
import io.quiet.auth.domain.hashPin
import io.quiet.auth.session.SessionLockController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

data class PinUiState(
    val isLoading: Boolean = true,
    val hasPin: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isUnlocked: Boolean = false,
    val isBiometricSupported: Boolean = false,
    val hasBiometricUnlock: Boolean = false,
    val sessionPin: String? = null,
)

/** Backup / biometrics require an authenticated session; without PIN protection there is no PIN secret. */
val PinUiState.sessionReadyForSensitiveActions: Boolean
    get() = !isPinEnabled || (isUnlocked && sessionPin != null)

class PinViewModel(
    private val pinRepository: PinRepository,
    private val sessionLock: SessionLockController,
    private val activityRef: WeakReference<FragmentActivity>,
) : ViewModel() {

    private val _state = MutableStateFlow(PinUiState())
    val state: StateFlow<PinUiState> = _state.asStateFlow()

    private var storedHash: String? = null

    init {
        viewModelScope.launch {
            sessionLock.locked.collect { locked ->
                if (locked) {
                    if (_state.value.isPinEnabled) {
                        _state.value = _state.value.copy(isUnlocked = false, sessionPin = null)
                    }
                    sessionLock.consumeLockSignal()
                }
            }
        }
        refreshFromStorage()
    }

    private fun refreshFromStorage() {
        val activity = activityRef.get()
        var hash = pinRepository.loadPinHash()
        var pinEnabled = pinRepository.loadPinProtectionEnabled()

        if (pinEnabled && hash == null) {
            pinRepository.setPinProtectionEnabled(false)
        }
        if (!pinEnabled && hash != null) {
            pinRepository.clearPin()
        }

        hash = pinRepository.loadPinHash()
        pinEnabled = pinRepository.loadPinProtectionEnabled()
        val biometricEnabled = pinRepository.isBiometricEnabled()
        val biometricSupported = activity?.let {
            BiometricAuth.isAvailable(it) == BiometricAvailability.AVAILABLE
        } ?: false
        storedHash = hash
        val unlocked = !pinEnabled
        _state.value = PinUiState(
            isLoading = false,
            hasPin = hash != null,
            isPinEnabled = pinEnabled,
            isUnlocked = unlocked,
            isBiometricSupported = biometricSupported,
            hasBiometricUnlock = biometricEnabled,
            sessionPin = null,
        )
    }

    fun validatePinFormat(pin: String): Boolean = PIN_REGEX.matches(pin)

    fun verifyPin(pin: String): Boolean = storedHash?.let { hashPin(pin) == it } == true

    /**
     * Creates PIN credentials and enables protection (onboarding or Settings).
     */
    fun setPin(pin: String): Boolean {
        if (!PIN_REGEX.matches(pin)) return false
        val hash = hashPin(pin)
        return runCatching {
            pinRepository.savePinHash(hash)
            pinRepository.setPinProtectionEnabled(true)
            storedHash = hash
            tryEnableBiometricUnlock(pin)
            _state.value = _state.value.copy(
                hasPin = true,
                isPinEnabled = true,
                isUnlocked = true,
                sessionPin = pin,
                isBiometricSupported = activityRef.get()?.let {
                    BiometricAuth.isAvailable(it) == BiometricAvailability.AVAILABLE
                } ?: false,
                hasBiometricUnlock = pinRepository.isBiometricEnabled(),
            )
            true
        }.getOrElse { false }
    }

    fun unlockWithPin(pin: String): Boolean {
        if (!verifyPin(pin)) return false
        _state.value = _state.value.copy(
            isUnlocked = true,
            sessionPin = pin,
            isBiometricSupported = activityRef.get()?.let {
                BiometricAuth.isAvailable(it) == BiometricAvailability.AVAILABLE
            } ?: false,
        )
        return true
    }

    suspend fun unlockWithBiometrics(
        title: String,
        cancelLabel: String,
    ): Boolean {
        if (!_state.value.isPinEnabled || storedHash == null || !pinRepository.isBiometricEnabled()) return false
        val activity = activityRef.get() ?: return false
        val result = BiometricAuth.authenticate(activity, title = title, cancelLabel = cancelLabel)
        if (result is BiometricResult.Error) return false
        val pin = pinRepository.loadBiometricPin() ?: return false
        return unlockWithPin(pin)
    }

    suspend fun setBiometricUnlockEnabled(
        enabled: Boolean,
        title: String,
        cancelLabel: String,
    ): Boolean {
        if (!_state.value.isPinEnabled) return false
        if (!enabled) {
            pinRepository.clearBiometric()
            _state.value = _state.value.copy(hasBiometricUnlock = false)
            return true
        }
        val pin = _state.value.sessionPin ?: return false
        return tryEnableBiometricUnlock(pin, title, cancelLabel)
    }

    private fun tryEnableBiometricUnlock(pin: String): Boolean {
        if (!_state.value.isPinEnabled) return false
        val activity = activityRef.get() ?: return false
        if (BiometricAuth.isAvailable(activity) != BiometricAvailability.AVAILABLE) {
            pinRepository.clearBiometric()
            _state.value = _state.value.copy(hasBiometricUnlock = false)
            return false
        }
        pinRepository.saveBiometricPin(pin)
        _state.value = _state.value.copy(hasBiometricUnlock = true)
        return true
    }

    private suspend fun tryEnableBiometricUnlock(
        pin: String,
        title: String,
        cancelLabel: String,
    ): Boolean {
        if (!_state.value.isPinEnabled) return false
        val activity = activityRef.get() ?: return false
        if (BiometricAuth.isAvailable(activity) != BiometricAvailability.AVAILABLE) {
            return false
        }
        val result = BiometricAuth.authenticate(activity, title = title, cancelLabel = cancelLabel)
        if (result is BiometricResult.Error) return false
        pinRepository.saveBiometricPin(pin)
        _state.value = _state.value.copy(hasBiometricUnlock = true)
        return true
    }

    fun lock() {
        _state.value = _state.value.copy(isUnlocked = false, sessionPin = null)
    }

    fun disablePinProtectionAfterVerification(pin: String): Boolean {
        if (!_state.value.isPinEnabled || !verifyPin(pin)) return false
        return runCatching {
            pinRepository.clearPin()
            storedHash = null
            applyUnprotectedState()
            true
        }.getOrElse { false }
    }

    fun removePin(): Boolean = runCatching {
        pinRepository.clearPin()
        storedHash = null
        applyUnprotectedState()
        true
    }.getOrElse { false }

    private fun applyUnprotectedState() {
        val biometricSupported = activityRef.get()?.let {
            BiometricAuth.isAvailable(it) == BiometricAvailability.AVAILABLE
        } ?: false
        _state.value = PinUiState(
            isLoading = false,
            hasPin = false,
            isPinEnabled = false,
            isUnlocked = true,
            isBiometricSupported = biometricSupported,
            hasBiometricUnlock = false,
            sessionPin = null,
        )
    }

    fun removeBiometrics(): Boolean = runCatching {
        pinRepository.clearBiometric()
        _state.value = _state.value.copy(hasBiometricUnlock = false)
        true
    }.getOrElse { false }

    companion object {
        fun factory(app: QuietAuthApp, activity: FragmentActivity) = viewModelFactory {
            initializer {
                PinViewModel(
                    pinRepository = app.pinRepository,
                    sessionLock = app.sessionLockController,
                    activityRef = WeakReference(activity),
                )
            }
        }
    }
}
