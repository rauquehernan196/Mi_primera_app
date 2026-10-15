package com.example.miprimeraapp.ui.navigation

sealed interface Screen {
    data object Splash : Screen
    data object Login : Screen
    data class Home(val username: String) : Screen
    data class Profile(val username: String) : Screen
    data object Settings : Screen
}
