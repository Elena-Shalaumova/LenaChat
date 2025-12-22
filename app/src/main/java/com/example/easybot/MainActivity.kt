package com.example.easybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.easybot.data.auth.AuthDataStore
import com.example.easybot.data.local.ThemePreferences
import com.example.easybot.data.repository.AuthRepository
import com.example.easybot.screens.navigation.MyAppNavigation
import com.example.easybot.screens.theme.EasyBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val systemDarkTheme = isSystemInDarkTheme()

            var isDarkTheme by rememberSaveable {
                mutableStateOf(ThemePreferences.getTheme(context, systemDarkTheme))
            }

            // Репозиторий авторизации (DataStore)
            val authRepository = remember {
                AuthRepository(AuthDataStore(context.applicationContext))
            }

            // Флаг авторизации из DataStore
            val isAuthorized by authRepository.isAuthorizedFlow.collectAsState(initial = false)

            EasyBotTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                val navController = rememberNavController()

                MyAppNavigation(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = it
                        ThemePreferences.saveTheme(context, it)
                    },
                    isAuthorized = isAuthorized
                )
            }
        }
    }
}
