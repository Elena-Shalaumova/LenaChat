package com.example.easybot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.easybot.presentation.navigation.MyAppNavigation
import com.example.easybot.presentation.feature_chat.ui.theme.EasyBotTheme
import com.example.easybot.data.local.prefs.ThemePreferences
import com.example.easybot.settings.UserSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val systemDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable {
                mutableStateOf(ThemePreferences.getTheme(context, systemDarkTheme))
            }
            EasyBotTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                val navController = rememberNavController()
                MyAppNavigation(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = it
                        ThemePreferences.saveTheme(context, it)
                    }
                )
            }
        }
    }
}

