package com.example.easybot

import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import com.example.easybot.ui.theme.EasyBotTheme

@Composable
fun MyAppNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Registrationpage, builder ={
        composable(Routes.Registrationpage,){
            Registrationpage(navController)
        }
        composable(Routes.ChatPage,){
            val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
            setContent {
                 EasyBotTheme {
                     Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                         ChatPage(modifier = Modifier.padding(innerPadding), chatViewModel)
                     }
                 }
    }
}