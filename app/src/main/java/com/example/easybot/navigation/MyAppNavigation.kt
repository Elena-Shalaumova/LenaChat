package com.example.easybot.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.easybot.screens.theme.ChatListViewModel
import com.example.easybot.screens.ChatPage
import com.example.easybot.screens.RegistrationPage
import com.example.easybot.screens.ChatListScreen
import com.example.easybot.screens.ChatViewModel
import com.example.easybot.screens.theme.SignUpPage

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
            val vm: ChatListViewModel = viewModel()
            ChatListScreen(navController = navController, viewModel = vm)
        }

        // экран конкретного чата, с параметром chatId
        composable(
            route = "chat/{chatId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getLong("chatId") ?: return@composable
            val chatVm: ChatViewModel = viewModel(
                key = "chat_$chatId"
            )
            ChatPage(
                chatId = chatId,
                viewModel = chatVm
            )
        }
    }
}