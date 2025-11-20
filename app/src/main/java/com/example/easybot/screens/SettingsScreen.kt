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
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val userLogin = UserSession.login ?: "N/A"
    var streamEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val api = remember { provideApi() }
    val userId = (UserSession.userId ?: 0L).toInt()
    var selectedModel by remember { mutableStateOf("") }
    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var ollamaVersion by remember { mutableStateOf<String?>(null) }
    var ollamaModels by remember { mutableStateOf<List<String>>(emptyList()) }


    LaunchedEffect(userId) {
//        try {
//            val settings = api.getSettings(userId)
//            streamEnabled = settings.stream        // включить/выключить тумблер
//            selectedModel = settings.model ?: ""
//        } catch (e: Exception) {
//            // если настроек нет — оставляем значения по умолчанию
//            e.printStackTrace()
//        }
//        try {
//            val versionDto = api.getOllamaVersion()
//            ollamaVersion = versionDto.version
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ollamaVersion = "неизвестна"
//        }
        // 1. Сначала загружаем список моделей из Ollama
        try {
            val modelsDto = api.getAvailableModels()          // <- твой метод
            ollamaModels = modelsDto               // или как у тебя поле называется
        } catch (e: Exception) {
            e.printStackTrace()
            ollamaModels = emptyList()
        }

        // 2. Пытаемся получить настройки пользователя
        try {
            val settings = api.getSettings(userId)

            streamEnabled = settings.stream
            // если в настройках модель пустая — берём первую из списка
            selectedModel = settings.model?.takeIf { it.isNotBlank() }
                ?: ollamaModels.firstOrNull().orEmpty()

        } catch (e: retrofit2.HttpException) {
            // 404 – настроек нет, создаём дефолтные
            if (e.code() == 404) {
                val defaultModel = ollamaModels.firstOrNull()

                // выставляем в UI
                selectedModel = defaultModel.orEmpty()
                streamEnabled = false

                if (defaultModel != null) {
                    // 3. Отправляем на бэк создание настроек по умолчанию
                    try {
                        val response = api.saveSettings(
                            SettingsRequest(id = userId, stream = streamEnabled, model = selectedModel)
                        )
                    } catch (saveEx: Exception) {
                        saveEx.printStackTrace()
                    }
                }
            } else {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Версию Ollama (как у тебя было)
        try {
            val versionDto = api.getOllamaVersion()
            ollamaVersion = versionDto.version
        } catch (e: Exception) {
            e.printStackTrace()
            ollamaVersion = "неизвестна"
        }
    }

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
                fontWeight = FontWeight.Normal,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))


            Text(
                text = "Версия Ollama: $ollamaVersion",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

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

            Text(
                text = "Модель ИИ (Ollama)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = isModelDropdownExpanded,
                onExpandedChange = { isModelDropdownExpanded = !isModelDropdownExpanded }
            ) {
                TextField(
                    value = selectedModel,
                    onValueChange = { }, // только выбор из списка
                    readOnly = true,
                    label = { Text("Выберите модель") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isModelDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isModelDropdownExpanded,
                    onDismissRequest = { isModelDropdownExpanded = false }
                ) {
                    ollamaModels.forEach { modelName ->
                        DropdownMenuItem(
                            text = { Text(modelName) },
                            onClick = {
                                selectedModel = modelName
                                isModelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Увеличим отступ перед кнопками

            // Оборачиваем кнопки в Row для горизонтального расположения
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Пространство между кнопками
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Сброс" (муляж)
                OutlinedButton(
                    onClick = {
                        // TODO: Добавить логику сброса настроек
                        Toast.makeText(context, "Сброс (пока не работает)", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f), // Занимает половину доступного пространства
                    enabled = !isLoading
                ) {
                    Text("Сброс")
                }

                // Существующая кнопка "Сохранить"
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
                                    SettingsRequest(id = userId, stream = streamEnabled, model = selectedModel)
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
                    modifier = Modifier.weight(1f), // Занимает вторую половину пространства
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
}
