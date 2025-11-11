package com.example.easybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.easybot.navigation.MyAppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        //val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        setContent {
           // EasyBotTheme {
              //  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                 //   ChatPage(modifier = Modifier.padding(innerPadding),chatViewModel)
            val navController = rememberNavController()
            MyAppNavigation(navController)
                }
            }
        }
   // }
//}

