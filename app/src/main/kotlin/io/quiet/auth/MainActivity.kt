package io.quiet.auth

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import io.quiet.auth.ui.nav.AppNav
import io.quiet.auth.ui.theme.QuietAuthTheme
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

/**
 * FragmentActivity is required for AndroidX BiometricPrompt to attach a host fragment when
 * the system biometric dialog is shown.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as QuietAuthApp
        setContent {
            val pinViewModel: PinViewModel = viewModel(factory = PinViewModel.factory(app, this))
            val twoFAViewModel: TwoFAViewModel = viewModel(factory = TwoFAViewModel.factory(app))
            QuietAuthTheme {
                AppNav(
                    pinViewModel = pinViewModel,
                    twoFAViewModel = twoFAViewModel,
                    backupIO = app.backupIO,
                    pinRepository = app.pinRepository,
                )
            }
        }
    }
}
