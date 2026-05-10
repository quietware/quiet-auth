package dev.calmauth

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dev.calmauth.data.BackupIO
import dev.calmauth.data.PinRepository
import dev.calmauth.data.SecureStorage
import dev.calmauth.data.TokenRepository
import dev.calmauth.session.SessionLockController

/**
 * Tiny composition root. Tying our handful of singletons to the application avoids pulling in
 * a full DI library — the surface area is small enough that constructor injection from
 * `MainActivity` reads as the simplest contract.
 */
class CalmAuthApp : Application() {

    lateinit var secureStorage: SecureStorage
        private set
    lateinit var tokenRepository: TokenRepository
        private set
    lateinit var pinRepository: PinRepository
        private set
    lateinit var sessionLockController: SessionLockController
        private set
    lateinit var backupIO: BackupIO
        private set

    override fun onCreate() {
        super.onCreate()
        secureStorage = SecureStorage.create(this)
        tokenRepository = TokenRepository(secureStorage)
        pinRepository = PinRepository(secureStorage)
        sessionLockController = SessionLockController()
        ProcessLifecycleOwner.get().lifecycle.addObserver(sessionLockController)
        backupIO = BackupIO(this, sessionLockController)
    }
}
