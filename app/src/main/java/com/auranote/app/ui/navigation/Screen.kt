package com.auranote.app.ui.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Library : Screen("library")
    object Record : Screen("record")
    object Settings : Screen("settings")

    object Detail : Screen("detail/{recordingId}") {
        fun createRoute(recordingId: Long) = "detail/$recordingId"
    }
}
