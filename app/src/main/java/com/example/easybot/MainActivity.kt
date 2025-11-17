package com.example.easybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.easybot.navigation.MyAppNavigation
import com.example.easybot.screens.theme.EasyBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
           EasyBotTheme(dynamicColor = false) { // Применяем нашу тему и отключаем динамический цвет
                val navController = rememberNavController()
                MyAppNavigation(navController)
           }
        }
    }
}
