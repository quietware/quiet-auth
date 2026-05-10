package dev.calmauth.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.calmauth.data.BackupIO
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

@Composable
fun AppNav(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    backupIO: BackupIO,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onBegin = { navController.navigate(Routes.PIN) },
                onDeveloperMode = { navController.navigate(Routes.DEVELOPER_MODE) },
            )
        }
        composable(Routes.PIN) {
            PinScreen(
                viewModel = pinViewModel,
                onUnlocked = {
                    navController.navigate(Routes.TWOFAS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = false }
                        launchSingleTop = true
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
                onLocked = {
                    navController.navigate(Routes.PIN) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                },
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
                onLocked = {
                    navController.navigate(Routes.PIN) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                },
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
                onLocked = {
                    navController.navigate(Routes.PIN) {
                        popUpTo(Routes.PIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.DEVELOPER_MODE) {
            DeveloperModeScreen(
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
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
