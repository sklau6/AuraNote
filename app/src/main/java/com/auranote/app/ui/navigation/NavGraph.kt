package com.auranote.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.auranote.app.ui.screens.DetailScreen
import com.auranote.app.ui.screens.MainScreen
import com.auranote.app.ui.screens.OnboardingScreen
import com.auranote.app.ui.screens.RecordScreen

@Composable
fun AuraNoteNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScreen(
                onNavigateToDetail = { recordingId ->
                    navController.navigate(Screen.Detail.createRoute(recordingId))
                },
                onNavigateToRecord = {
                    navController.navigate(Screen.Record.route)
                }
            )
        }

        composable(
            route = Screen.Record.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            }
        ) {
            RecordScreen(
                onBack = { navController.popBackStack() },
                onRecordingComplete = { recordingId ->
                    navController.navigate(Screen.Detail.createRoute(recordingId)) {
                        popUpTo(Screen.Record.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("recordingId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeIn(tween(350))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                ) + fadeOut(tween(350))
            }
        ) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getLong("recordingId") ?: 0L
            DetailScreen(
                recordingId = recordingId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
