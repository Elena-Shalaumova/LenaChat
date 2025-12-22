package com.example.easybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.easybot.core.session.UserSession
import com.example.easybot.data.auth.AuthDataStore
import com.example.easybot.data.local.ThemePreferences
import com.example.easybot.data.repository.AuthRepository
import com.example.easybot.screens.navigation.MyAppNavigation
import com.example.easybot.screens.theme.EasyBotTheme
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val systemDarkTheme = isSystemInDarkTheme()

            var isDarkTheme by rememberSaveable {
                mutableStateOf(ThemePreferences.getTheme(context, systemDarkTheme))
            }

            val authRepository = remember {
                AuthRepository(AuthDataStore(context.applicationContext))
            }

            // null = DataStore ещё не прочитан (важно, чтобы RootScreen не редиректил раньше времени)
            val authState: Boolean? by authRepository.isAuthorizedFlow
                .map { it as Boolean? }
                .collectAsState(initial = null)

            // Восстанавливаем userId в память (UserSession) после перезапуска
            val savedUserId by authRepository.userIdFlow.collectAsState(initial = null)

            LaunchedEffect(savedUserId) {
                UserSession.userId = savedUserId?.toLong()
            }

            EasyBotTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                val navController = rememberNavController()

                MyAppNavigation(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = it
                        ThemePreferences.saveTheme(context, it)
                    },
                    authState = authState
                )
            }
        }
    }
}
