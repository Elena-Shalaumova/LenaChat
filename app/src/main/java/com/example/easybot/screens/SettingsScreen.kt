package com.example.easybot.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.easybot.UserSession
import com.example.easybot.SettingsRequest
import com.example.easybot.provideApi
import com.example.easybot.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val userLogin = UserSession.login ?: "N/A"
    var streamEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val api = remember { provideApi() }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("Настройки", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // 1. Увеличиваем отступ до ~3см
                .padding(horizontal = 48.dp)
                // Добавляем отступ снизу, чтобы кнопка не прилипала к краю
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.Start,
            // 2. Смещаем весь контент вниз
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Логин: $userLogin",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = "Потоковая передача ответов",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "Включите, чтобы получать ответы в режиме реального времени",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = streamEnabled,
                    onCheckedChange = { streamEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate(Routes.AdminPanel) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text("Админ-панель")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = UserSession.userId?.toInt()
                    if (userId == null) {
                        Toast.makeText(context, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val response = api.saveSettings(
                                SettingsRequest(id = userId, stream = streamEnabled)
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Настройки сохранены", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Не удалось сохранить настройки", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Ошибка: ${e.message ?: "неизвестная"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(if (isLoading) "Сохранение..." else "Сохранить")
            }
        }
    }
}