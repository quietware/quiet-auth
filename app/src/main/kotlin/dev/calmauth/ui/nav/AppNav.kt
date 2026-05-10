package dev.calmauth.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.calmauth.data.BackupIO
import dev.calmauth.data.PinRepository
import dev.calmauth.ui.screens.AddTwoFAQrScreen
import dev.calmauth.ui.screens.AddTwoFAScreen
import dev.calmauth.ui.screens.BackupProcessingScreen
import dev.calmauth.ui.screens.DeveloperModeScreen
import dev.calmauth.ui.screens.OnboardingScreen
import dev.calmauth.ui.screens.PinScreen
import dev.calmauth.ui.screens.SettingsScreen
import dev.calmauth.ui.screens.TokenDetailsScreen
import dev.calmauth.ui.screens.TwoFAsScreen
import dev.calmauth.ui.viewmodel.PinViewModel
import dev.calmauth.ui.viewmodel.TwoFAViewModel

private fun NavController.navigateToUnlockPin() {
    navigate(Routes.pin(PinRouteMode.UNLOCK)) {
        popUpTo(Routes.TWOFAS) { inclusive = false }
    }
}

@Composable
fun AppNav(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    backupIO: BackupIO,
    pinRepository: PinRepository,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.START) {
        composable(Routes.START) {
            val pinState by pinViewModel.state.collectAsState()
            var bootstrapped by remember { mutableStateOf(false) }
            LaunchedEffect(pinState.isLoading) {
                if (bootstrapped || pinState.isLoading) return@LaunchedEffect
                bootstrapped = true
                val onboardingDone = pinRepository.isOnboardingCompleted()
                val target = when {
                    !onboardingDone -> Routes.ONBOARDING
                    pinState.isPinEnabled && !pinState.isUnlocked ->
                        Routes.pin(PinRouteMode.UNLOCK)
                    else -> Routes.TWOFAS
                }
                navController.navigate(target) {
                    popUpTo(Routes.START) { inclusive = true }
                }
            }
            Box(Modifier.fillMaxSize())
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinueWithoutPin = {
                    pinRepository.setOnboardingCompleted()
                    navController.navigate(Routes.TWOFAS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onProtectWithPin = {
                    navController.navigate(Routes.pin(PinRouteMode.SETUP))
                },
                onDeveloperMode = { navController.navigate(Routes.DEVELOPER_MODE) },
            )
        }
        composable(
            route = Routes.PIN_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_PIN_MODE) { type = NavType.StringType }),
        ) { entry ->
            val mode = entry.arguments?.getString(Routes.ARG_PIN_MODE)
                .takeUnless { it.isNullOrEmpty() }
                ?: PinRouteMode.UNLOCK
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            PinScreen(
                viewModel = pinViewModel,
                mode = mode,
                onFinished = {
                    when (mode) {
                        PinRouteMode.VERIFY_DISABLE -> navController.popBackStack()
                        PinRouteMode.SETUP -> when (previousRoute) {
                            Routes.SETTINGS -> navController.popBackStack()
                            else -> {
                                pinRepository.setOnboardingCompleted()
                                navController.navigate(Routes.TWOFAS) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            }
                        }
                        else -> navController.navigate(Routes.TWOFAS) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable(Routes.TWOFAS) {
            TwoFAsScreen(
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                onTokenClick = { id -> navController.navigate(Routes.tokenDetails(id)) },
                onAdd = { navController.navigate(Routes.ADD_TWOFA_QR) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onLocked = { navController.navigateToUnlockPin() },
            )
        }
        composable(
            route = Routes.TOKEN_DETAILS,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Routes.ARG_ID).orEmpty()
            TokenDetailsScreen(
                tokenId = id,
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                onBack = { navController.popBackStack(Routes.TWOFAS, inclusive = false) },
                onLocked = { navController.navigateToUnlockPin() },
            )
        }
        composable(Routes.ADD_TWOFA) {
            AddTwoFAScreen(
                twoFAViewModel = twoFAViewModel,
                onSaved = {
                    navController.navigate(Routes.TWOFAS) {
                        popUpTo(Routes.TWOFAS) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(Routes.ADD_TWOFA_QR) {
            AddTwoFAQrScreen(
                twoFAViewModel = twoFAViewModel,
                onAddManually = { navController.navigate(Routes.ADD_TWOFA) },
                onCancel = { navController.popBackStack() },
                onAdded = {
                    navController.navigate(Routes.TWOFAS) {
                        popUpTo(Routes.TWOFAS) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                pinViewModel = pinViewModel,
                onBack = { navController.popBackStack() },
                onCreateBackup = { navController.navigate(Routes.backupProcessing("create")) },
                onRestoreBackup = { navController.navigate(Routes.backupProcessing("restore")) },
                onEnablePinProtection = {
                    navController.navigate(Routes.pin(PinRouteMode.SETUP))
                },
                onDisablePinProtection = {
                    navController.navigate(Routes.pin(PinRouteMode.VERIFY_DISABLE))
                },
            )
        }
        composable(
            route = Routes.BACKUP_PROCESSING,
            arguments = listOf(navArgument(Routes.ARG_ACTION) { type = NavType.StringType }),
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString(Routes.ARG_ACTION) ?: "create"
            BackupProcessingScreen(
                action = action,
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                backupIO = backupIO,
                onFinished = {
                    navController.navigate(Routes.TWOFAS) {
                        popUpTo(Routes.TWOFAS) { inclusive = true }
                    }
                },
                onLocked = { navController.navigateToUnlockPin() },
            )
        }
        composable(Routes.DEVELOPER_MODE) {
            DeveloperModeScreen(
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                pinRepository = pinRepository,
                onBack = { navController.popBackStack() },
                onAfterReset = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
    }
}
