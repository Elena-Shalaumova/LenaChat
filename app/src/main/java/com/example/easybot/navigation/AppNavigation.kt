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
fun AppNavigation(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
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
            //SettingsScreen(navController = navController)
            SettingsScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }

        composable(Routes.Help) {
            HelpScreen(navController)
        }

        composable(
            route = Routes.Chat, // "chat/{chatId}/{chatTitle}/{incognitoFlag}"
            arguments = listOf(
                navArgument("chatId")       { type = NavType.LongType   },
                navArgument("chatTitle")    { type = NavType.StringType },
                navArgument("incognitoFlag"){ type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->

            val chatId = backStackEntry.arguments?.getLong("chatId") ?: return@composable
            val chatTitle = backStackEntry.arguments?.getString("chatTitle") ?: "Чат"
            val incognitoFlag = backStackEntry.arguments?.getInt("incognitoFlag") ?: 0
            // 0 = обычный чат, 1 = инкогнито

            val chatVm: ChatViewModel = viewModel(key = "chat_${chatId}")
            ChatPage(
                chatId = chatId,
                chatTitle = chatTitle,
                incognitoFlag = incognitoFlag,
                viewModel = chatVm
            )
        }
    }
}
