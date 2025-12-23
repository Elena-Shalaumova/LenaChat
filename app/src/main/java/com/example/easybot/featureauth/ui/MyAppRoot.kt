package com.example.easybot.featureauth.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.easybot.featurechat.ui.ChatListScreen
import com.example.easybot.featurechat.ui.ChatPage
import com.example.easybot.featurechat.vm.ChatViewModel
import com.example.easybot.featuresettings.ui.SettingsScreen
import com.example.easybot.screens.HelpScreen
import com.example.easybot.screens.navigation.Routes
import androidx.navigation.compose.rememberNavController
import com.example.easybot.screens.navigation.MyAppNavigation

@Composable
fun MyAppRoot(
    startDestination: String,
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {}
) {
    val navController = rememberNavController()

    MyAppNavigation(
        navController = navController,
        startDestination = startDestination,
        isDarkTheme = isDarkTheme,
        onThemeToggle = onThemeToggle
    )
}