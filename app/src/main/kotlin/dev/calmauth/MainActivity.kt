package dev.calmauth

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.calmauth.ui.nav.AppNav
import dev.calmauth.ui.theme.CalmAuthTheme
import dev.calmauth.ui.viewmodel.PinViewModel
import dev.calmauth.ui.viewmodel.TwoFAViewModel

/**
 * FragmentActivity is required for AndroidX BiometricPrompt to attach a host fragment when
 * the system biometric dialog is shown.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as CalmAuthApp
        setContent {
            val pinViewModel: PinViewModel = viewModel(factory = PinViewModel.factory(app, this))
            val twoFAViewModel: TwoFAViewModel = viewModel(factory = TwoFAViewModel.factory(app))
            CalmAuthTheme {
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
