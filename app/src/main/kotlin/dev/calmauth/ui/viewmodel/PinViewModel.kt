package dev.calmauth.ui.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.calmauth.CalmAuthApp
import dev.calmauth.auth.BiometricAuth
import dev.calmauth.auth.BiometricAvailability
import dev.calmauth.auth.BiometricResult
import dev.calmauth.data.PinRepository
import dev.calmauth.domain.PIN_REGEX
import dev.calmauth.domain.hashPin
import dev.calmauth.session.SessionLockController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

data class PinUiState(
    val isLoading: Boolean = true,
    val hasPin: Boolean = false,
    val isUnlocked: Boolean = false,
    val isBiometricSupported: Boolean = false,
    val hasBiometricUnlock: Boolean = false,
    val sessionPin: String? = null,
)

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
                    _state.value = _state.value.copy(isUnlocked = false, sessionPin = null)
                    sessionLock.consumeLockSignal()
                }
            }
        }
        refreshFromStorage()
    }

    private fun refreshFromStorage() {
        val activity = activityRef.get()
        val hash = pinRepository.loadPinHash()
        val biometricEnabled = pinRepository.isBiometricEnabled()
        val biometricSupported = activity?.let {
            BiometricAuth.isAvailable(it) == BiometricAvailability.AVAILABLE
        } ?: false
        storedHash = hash
        _state.value = PinUiState(
            isLoading = false,
            hasPin = hash != null,
            isUnlocked = false,
            isBiometricSupported = biometricSupported,
            hasBiometricUnlock = biometricEnabled,
        )
    }

    fun validatePinFormat(pin: String): Boolean = PIN_REGEX.matches(pin)

    fun verifyPin(pin: String): Boolean = storedHash?.let { hashPin(pin) == it } == true

    fun setPin(pin: String): Boolean {
        if (!PIN_REGEX.matches(pin)) return false
        val hash = hashPin(pin)
        return runCatching {
            pinRepository.savePinHash(hash)
            storedHash = hash
            tryEnableBiometricUnlock(pin)
            _state.value = _state.value.copy(
                hasPin = true,
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
        if (storedHash == null || !pinRepository.isBiometricEnabled()) return false
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
        if (!enabled) {
            pinRepository.clearBiometric()
            _state.value = _state.value.copy(hasBiometricUnlock = false)
            return true
        }
        val pin = _state.value.sessionPin ?: return false
        return tryEnableBiometricUnlock(pin, title, cancelLabel)
    }

    private fun tryEnableBiometricUnlock(pin: String): Boolean {
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

    fun removePin(): Boolean = runCatching {
        pinRepository.clearPin()
        storedHash = null
        _state.value = PinUiState(
            isLoading = false,
            hasPin = false,
            isUnlocked = false,
            isBiometricSupported = activityRef.get()?.let {
                BiometricAuth.isAvailable(it) == BiometricAvailability.AVAILABLE
            } ?: false,
            hasBiometricUnlock = false,
        )
        true
    }.getOrElse { false }

    fun removeBiometrics(): Boolean = runCatching {
        pinRepository.clearBiometric()
        _state.value = _state.value.copy(hasBiometricUnlock = false)
        true
    }.getOrElse { false }

    companion object {
        fun factory(app: CalmAuthApp, activity: FragmentActivity) = viewModelFactory {
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
