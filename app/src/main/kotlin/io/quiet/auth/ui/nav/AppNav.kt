package io.quiet.auth.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.quiet.auth.R
import io.quiet.auth.data.BackupIO
import io.quiet.auth.data.PinRepository
import io.quiet.auth.ui.screens.AddTwoFAQrScreen
import io.quiet.auth.ui.screens.AddTwoFAScreen
import io.quiet.auth.ui.screens.BackupProcessingScreen
import io.quiet.auth.ui.screens.DangerZoneScreen
import io.quiet.auth.ui.screens.DeveloperModeScreen
import io.quiet.auth.ui.screens.OnboardingScreen
import io.quiet.auth.ui.screens.PinScreen
import io.quiet.auth.ui.screens.SettingsBackupScreen
import io.quiet.auth.ui.screens.SettingsSecurityScreen
import io.quiet.auth.ui.screens.SettingsScreen
import io.quiet.auth.ui.screens.TokenDetailsScreen
import io.quiet.auth.ui.screens.TwoFAsScreen
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

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
                            Routes.SETTINGS, Routes.SETTINGS_SECURITY ->
                                navController.popBackStack()
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
                onBack = { navController.popBackStack() },
                onSecurity = { navController.navigate(Routes.SETTINGS_SECURITY) },
                onBackup = { navController.navigate(Routes.SETTINGS_BACKUP) },
                onDangerZone = { navController.navigate(Routes.SETTINGS_DANGER_ZONE) },
            )
        }
        composable(Routes.SETTINGS_SECURITY) {
            SettingsSecurityScreen(
                pinViewModel = pinViewModel,
                onBack = { navController.popBackStack() },
                onEnablePinProtection = {
                    navController.navigate(Routes.pin(PinRouteMode.SETUP))
                },
                onDisablePinProtection = {
                    navController.navigate(Routes.pin(PinRouteMode.VERIFY_DISABLE))
                },
            )
        }
        composable(Routes.SETTINGS_BACKUP) {
            SettingsBackupScreen(
                pinViewModel = pinViewModel,
                onBack = { navController.popBackStack() },
                onCreateBackup = { navController.navigate(Routes.backupProcessing("create")) },
                onRestoreBackup = { navController.navigate(Routes.backupProcessing("restore")) },
            )
        }
        composable(Routes.SETTINGS_DANGER_ZONE) {
            DangerZoneScreen(
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                pinRepository = pinRepository,
                onBack = { navController.popBackStack() },
                onAfterReset = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                backButtonLabelRes = R.string.settingsBack,
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
                    val popped = navController.popBackStack(Routes.SETTINGS_BACKUP, inclusive = false)
                    if (!popped) {
                        navController.navigate(Routes.TWOFAS) {
                            popUpTo(Routes.TWOFAS) { inclusive = true }
                        }
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

    val activity = LocalContext.current as FragmentActivity
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BackHandler(enabled = currentRoute == Routes.TWOFAS) {
        activity.finish()
    }
}
