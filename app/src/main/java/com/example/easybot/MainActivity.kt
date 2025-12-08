package com.example.easybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.easybot.navigation.MyAppNavigation
import com.example.easybot.screens.theme.EasyBotTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //EasyBotTheme(dynamicColor = false) {   // применяем наш Material3 UI kit
            val systemDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }

            EasyBotTheme(darkTheme = isDarkTheme, dynamicColor = false) {   // применяем наш Material3 UI kit
                val navController = rememberNavController()
                //MyAppNavigation(navController)
                MyAppNavigation(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = it }
                )
            }
        }
    }
}
