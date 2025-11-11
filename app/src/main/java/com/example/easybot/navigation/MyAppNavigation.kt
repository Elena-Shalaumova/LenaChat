package com.example.easybot.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.easybot.screens.ChatPage
import com.example.easybot.screens.RegistrationPage

@Composable
fun MyAppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Register
    ) {
        composable(Routes.Register) {
            RegistrationPage(navController)
        }

        // ОПРЕДЕЛЯЕМ АРГУМЕНТ username
        composable(
            route = "${Routes.Chat}/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Guest"
            ChatPage(username = username) // передаём в экран
        }
    }
}
