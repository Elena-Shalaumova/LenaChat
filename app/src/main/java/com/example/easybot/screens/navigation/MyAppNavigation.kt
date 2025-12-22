package com.example.easybot.screens.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.easybot.featureauth.ui.RegistrationPage
import com.example.easybot.featurechat.ui.ChatListScreen
import com.example.easybot.featurechat.ui.ChatPage
import com.example.easybot.featurechat.vm.ChatViewModel
import com.example.easybot.featuresettings.ui.SettingsScreen
import com.example.easybot.screens.*

@Composable
fun MyAppNavigation(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    //isAuthorized: Boolean,
    authState: Boolean?
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Root
    ) {
        composable(Routes.Root) {
            RootScreen(
                navController = navController,
                //isAuthorized = isAuthorized,
                authState = authState
            )
        }

        composable(Routes.Register) {
            RegistrationPage(nav = navController)
        }

        composable(Routes.ChatList) {
            ChatListScreen(navController = navController)
        }

        composable(Routes.Settings) {
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
                navArgument("chatId") { type = NavType.LongType },
                navArgument("chatTitle") { type = NavType.StringType },
                navArgument("incognitoFlag") { type = NavType.IntType; defaultValue = 0 }
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
