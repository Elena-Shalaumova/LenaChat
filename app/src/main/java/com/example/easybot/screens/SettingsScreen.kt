package com.example.easybot.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easybot.SettingsRequest
import com.example.easybot.UserSession
import com.example.easybot.provideApi
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    // Берём текущее значение из UserSession, чтобы при открытии экрана оно подставилось
    var apiBaseUrl by remember { mutableStateOf(UserSession.apiBaseUrl) }
    val userLogin = UserSession.login ?: "N/A"

    var streamEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isClearingChats by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val api = remember { provideApi(apiBaseUrl) }
    val userId = (UserSession.userId ?: 0L).toInt()

    // выбранная модель
    var selectedModel by remember {
        mutableStateOf(UserSession.selectedModel ?: "")
    }

    // 🔥 температура генерации
    var temperature by remember {
        mutableStateOf(UserSession.temperature ?: 0.7)
    }

    // 📏 максимальная длина ответа
    var maxTokens by remember {
        mutableStateOf(UserSession.maxTokens ?: 1024)
    }

    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var ollamaVersion by remember { mutableStateOf<String?>(null) }
    var ollamaModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableModels by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(userId) {

        // 1. Сначала загружаем список моделей из Ollama через твой WebAPI
        try {
            val models = api.getAvailableModels()   // с бэка → /api/tags
            ollamaModels = models
        } catch (e: Exception) {
            e.printStackTrace()
            ollamaModels = emptyList()
        }

        // 2. Пытаемся получить настройки пользователя
        try {
            val settings = api.getSettings(userId)

            streamEnabled = settings.stream
            selectedModel = settings.model ?: ""

            // новые поля
            temperature = settings.temperature ?: 0.7
            maxTokens = settings.maxTokens ?: 1024

            // запоминаем в сессии, чтобы чат видел актуальные значения
            UserSession.selectedModel = selectedModel
            UserSession.temperature = temperature
            UserSession.maxTokens = maxTokens

        } catch (e: HttpException) {
            // 404 – настроек ещё нет, создаём дефолтные
            if (e.code() == 404) {
                val defaultModel = ollamaModels.firstOrNull()

                streamEnabled = false
                selectedModel = defaultModel.orEmpty()

                // temperature / maxTokens уже имеют дефолты из remember

                if (defaultModel != null) {
                    try {
                        api.saveSettings(
                            SettingsRequest(
                                id = userId,
                                stream = streamEnabled,
                                model = selectedModel,
                                temperature = temperature,
                                maxTokens = maxTokens
                            )
                        )
                        UserSession.selectedModel = selectedModel
                        UserSession.temperature = temperature
                        UserSession.maxTokens = maxTokens
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

        // 3. Проверяем, есть ли выбранная модель в списке моделей из Ollama
        if (ollamaModels.isNotEmpty() && selectedModel.isNotBlank() && selectedModel !in ollamaModels) {
            val fallback = ollamaModels.first()

            Toast.makeText(
                context,
                "Модель \"$selectedModel\" не найдена в Ollama. Выбрана \"$fallback\".",
                Toast.LENGTH_LONG
            ).show()

            // обновляем UI
            selectedModel = fallback
            UserSession.selectedModel = fallback

            // И ВАЖНО: сразу же сохраняем исправленную модель в БД
            try {
                api.saveSettings(
                    SettingsRequest(
                        id = userId,
                        stream = streamEnabled,
                        model = fallback,
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 4. Версия Ollama (как у тебя было)
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
                .padding(horizontal = 48.dp)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.Start,
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

            // --- Новый блок для baseUrl --- //
            Text(
                text = "Строка подключения к API",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = apiBaseUrl,
                onValueChange = { apiBaseUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("http://192.168.3.8:5167/") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (apiBaseUrl.isNotBlank()) {
                        UserSession.apiBaseUrl = apiBaseUrl
                        Toast.makeText(
                            context,
                            "Строка подключения к API обновлена",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "URL не может быть пустым",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text("Сохранить")
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                // Меню выпадающего списка
                ExposedDropdownMenu(
                    expanded = isModelDropdownExpanded,
                    onDismissRequest = { isModelDropdownExpanded = false }
                ) {
                    ollamaModels.forEach { modelName ->
                        DropdownMenuItem(
                            text = { Text(modelName) },
                            onClick = {
                                selectedModel = modelName
                                UserSession.selectedModel = modelName
                                isModelDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка "Очистить все чаты"
            Button(
                onClick = {
                    val uid = UserSession.userId?.toInt()
                    if (uid == null) {
                        Toast.makeText(
                            context,
                            "Пользователь не авторизован",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isClearingChats = true
                        try {
                            val chats = api.getChats(uid)

                            if (chats.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "У вас нет чатов для удаления",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                chats.forEach { chat ->
                                    val response = api.deleteChat(chat.id)
                                    if (!response.isSuccessful) {
                                        throw IllegalStateException("Не удалось очистить чат ${chat.id}")
                                    }
                                }
                                Toast.makeText(
                                    context,
                                    "Все чаты удалены",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Не удалось удалить чаты: ${e.message ?: "неизвестная ошибка"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } finally {
                            isClearingChats = false
                        }
                    }
                },
                enabled = !isClearingChats && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            ) {
                Text(if (isClearingChats) "Очистка..." else "Очистить все чаты")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка "Сохранить" (общие настройки)
            Button(
                onClick = {
                    val uid = UserSession.userId?.toInt()
                    if (uid == null) {
                        Toast.makeText(
                            context,
                            "Пользователь не авторизован",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val response = api.saveSettings(
                                SettingsRequest(
                                    id = uid,
                                    stream = streamEnabled,
                                    model = selectedModel,
                                    temperature = temperature,
                                    maxTokens = maxTokens
                                )
                            )
                            if (response.isSuccessful) {
                                // обновим сессию
                                UserSession.selectedModel = selectedModel
                                UserSession.temperature = temperature
                                UserSession.maxTokens = maxTokens

                                Toast.makeText(
                                    context,
                                    "Настройки сохранены",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Не удалось сохранить настройки",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
