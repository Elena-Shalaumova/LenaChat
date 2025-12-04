package com.example.easybot.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.easybot.ChatListViewModel
import com.example.easybot.screens.*

@Composable
fun MyAppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Register
    ) {
        composable(Routes.Register) {
            RegistrationPage(nav = navController)
        }

        composable(Routes.ChatList) {
            ChatListScreen(navController = navController)
        }

        composable(Routes.Settings) {
            SettingsScreen(navController = navController)
        }

        composable(Routes.Help) { HelpScreen(navController) }

//        composable(Routes.AdminPanel) {
//            AdminPanelScreen(navController = navController)
//        }

        composable(
            route = Routes.Chat, // "chat/{chatId}/{chatTitle}"
            arguments = listOf(
                navArgument("chatId") { type = NavType.LongType },
                navArgument("chatTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getLong("chatId") ?: return@composable
            val chatTitle = backStackEntry.arguments?.getString("chatTitle") ?: "Чат"

            val chatVm: ChatViewModel = viewModel(key = "chat_$chatId")
            ChatPage(
                chatId = chatId,
                chatTitle = chatTitle, // Передаем название в UI
                viewModel = chatVm
            )
        }
    }
}