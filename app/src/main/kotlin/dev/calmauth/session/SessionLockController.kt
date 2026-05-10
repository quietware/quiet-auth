package dev.calmauth.session

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Locks the PIN session as soon as the application enters the background, mirroring the
 * `AppState.addEventListener('change', ...)` logic in [context/pin-context.tsx]. Callers can
 * defer the lock during legitimate flows that briefly leave the app (system file picker,
 * share sheet) by bracketing them with [beginDefer] / [endDefer].
 */
class SessionLockController : DefaultLifecycleObserver {

    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked

    private var deferCount = 0
    private var inBackground = false

    override fun onStart(owner: LifecycleOwner) {
        inBackground = false
    }

    override fun onStop(owner: LifecycleOwner) {
        inBackground = true
        if (deferCount == 0) {
            _locked.value = true
        }
    }

    fun beginDefer() {
        deferCount += 1
    }

    fun endDefer() {
        deferCount = maxOf(0, deferCount - 1)
        if (deferCount == 0 && inBackground) {
            _locked.value = true
        }
    }

    fun consumeLockSignal() {
        _locked.value = false
    }
}
