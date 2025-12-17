package com.example.easybot.featuresettings.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easybot.data.remote.api.SettingsRequest
import com.example.easybot.core.session.UserSession
import com.example.easybot.data.remote.api.provideApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.easybot.screens.navigation.Routes
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.easybot.featurechat.vm.ChatViewModel
import java.net.ConnectException
import java.net.SocketTimeoutException


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    var apiBaseUrl by remember { mutableStateOf(UserSession.apiBaseUrl) }
    val userLogin = UserSession.login ?: "N/A"

    var streamEnabled by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isClearingChats by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    //val api = remember { provideApi(apiBaseUrl) }
    val api = remember(apiBaseUrl) { provideApi(apiBaseUrl) }
    val userId = (UserSession.userId ?: 0L).toInt()
    val scope = rememberCoroutineScope()

    var selectedModel by remember { mutableStateOf(UserSession.selectedModel ?: "") }
    var temperature by remember { mutableStateOf(UserSession.temperature ?: 0.7) }
    var maxTokens by remember { mutableStateOf(UserSession.maxTokens ?: 1024) }

    var isModelDropdownExpanded by remember { mutableStateOf(false) }
    var ollamaVersion by remember { mutableStateOf<String?>(null) }
    var ollamaModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var ollamaError by remember { mutableStateOf<String?>(null) }

    var isCheckingOllama by remember { mutableStateOf(false) }
    var ollamaStatusText by remember { mutableStateOf<String?>(null) }
    //var oLlamaModels by remember { mutableStateOf<List<String>>(emptyList()) }

    var modelsExpanded by remember { mutableStateOf(false) }


    fun checkOllama(baseUrl: String) {
        val api = provideApi(baseUrl)

        scope.launch {
            isCheckingOllama = true
            try {
                val version = api.getOllamaVersion().version
                val models = api.getAvailableModels() // List<String>

                ollamaModels = models
                ollamaStatusText = "Ollama OK • v$version • моделей: ${models.size}"

                if (selectedModel.isBlank() || selectedModel !in models) {
                    selectedModel = models.firstOrNull().orEmpty()
                    UserSession.selectedModel = selectedModel
                }

                Toast.makeText(context, "Подключение к Ollama успешно", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                ollamaStatusText = "Ollama недоступна: ${e.message}"
                Toast.makeText(context, "Ollama недоступна", Toast.LENGTH_LONG).show()
            } finally {
                isCheckingOllama = false
            }
        }
    }


    // ---------- загрузка данных ----------
    LaunchedEffect(userId) {
        // 1. модели
        try {
            val models = api.getAvailableModels()
            ollamaModels = models
        } catch (e: Exception) {
            e.printStackTrace()
            ollamaModels = emptyList()
        }

        // 2. настройки пользователя
        try {
            val settings = api.getSettings(userId)

            streamEnabled = settings.stream
            selectedModel = settings.model ?: ""
            temperature = settings.temperature ?: 1.0
            maxTokens = settings.maxTokens ?: 1000

            UserSession.selectedModel = selectedModel
            UserSession.temperature = temperature
            UserSession.maxTokens = maxTokens

        } catch (e: HttpException) {
            if (e.code() == 404) {
                val defaultModel = ollamaModels.firstOrNull()
                streamEnabled = false
                selectedModel = defaultModel.orEmpty()

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

        // 3. если выбранной модели уже нет
        if (ollamaModels.isNotEmpty() &&
            selectedModel.isNotBlank() &&
            selectedModel !in ollamaModels
        ) {
            val fallback = ollamaModels.first()

            Toast.makeText(
                context,
                "Модель \"$selectedModel\" не найдена в Ollama. Выбрана \"$fallback\".",
                Toast.LENGTH_LONG
            ).show()

            selectedModel = fallback
            UserSession.selectedModel = fallback

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

        // 4. версия ollama
        try {
            val versionDto = api.getOllamaVersion()
            ollamaVersion = versionDto.version
        } catch (e: Exception) {
            e.printStackTrace()
            ollamaVersion = "неизвестна"
            ollamaError = when (e) {
                is ConnectException ->
                    "❌ Ollama недоступна — сервер не отвечает"
                is SocketTimeoutException ->
                    "⏱️ Ollama не успевает отвечать"
                is HttpException ->
                    "❌ Ошибка Ollama (код ${e.code()})"
                else ->
                    "⚠️ Не удалось подключиться к Ollama"
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                title = { Text("Настройки") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ------- АККАУНТ -------
            Text(
                text = "Аккаунт",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Логин",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userLogin,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ------- ТЕМА -------
            Text(
                text = "Оформление",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Тема оформления",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Переключайте между светлой и тёмной темами",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onThemeToggle
                    )
                }
            }

            // ------- OLLAMA -------
            Text(
                text = "Ollama",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Версия Ollama: ${ollamaVersion ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (ollamaError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = ollamaError!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Потоковая передача ответов",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Ответ появляется по мере генерации",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = streamEnabled,
                            onCheckedChange = { streamEnabled = it }
                        )
                    }


                    Button(
                        onClick = { checkOllama(apiBaseUrl) },
                        enabled = !isCheckingOllama,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (isCheckingOllama) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Проверяю...")
                        } else {
                            Text("Проверить подключение к Ollama")
                        }
                    }
                    ollamaStatusText?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }



                }
            }

            // ------- API BASE URL -------
            Text(
                text = "Подключение к API",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = apiBaseUrl,
                        onValueChange = { apiBaseUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Строка подключения к API") },
                        placeholder = { Text("http://10.16.77.51:5167/") }
                    )
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
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Сохранить")
                    }
                }
            }

            // ------- МОДЕЛЬ ИИ -------
            Text(
                text = "Модель ИИ (Ollama)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
//                    ExposedDropdownMenuBox(
//                        expanded = isModelDropdownExpanded,
//                        onExpandedChange = { isModelDropdownExpanded = !isModelDropdownExpanded }
//                    ) {
////                        TextField(
////                            value = selectedModel,
////                            onValueChange = {},
////                            readOnly = true,
////                            label = { Text("Выберите модель") },
//                        TextField(
//                            value = if (selectedModel.isBlank()) "Выберите модель" else selectedModel,
//                            onValueChange = {},
//                            readOnly = true,
//                            label = { Text("Выберите модель") },
//                            trailingIcon = {
//                                ExposedDropdownMenuDefaults.TrailingIcon(
//                                    expanded = isModelDropdownExpanded
//                                )
//                            },
//                            modifier = Modifier
//                                .menuAnchor()
//                                .fillMaxWidth()
//                        )
//                        ExposedDropdownMenu(
//                            expanded = isModelDropdownExpanded,
//                            onDismissRequest = { isModelDropdownExpanded = false }
//                        ) {
//                            ollamaModels.forEach { modelName ->
//                                DropdownMenuItem(
//                                    text = { Text(modelName) },
//                                    onClick = {
//                                        selectedModel = modelName
//                                        UserSession.selectedModel = modelName
//                                        isModelDropdownExpanded = false
//                                    }
//                                )
//                            }
//                        }
//                        if (ollamaModels.isEmpty()) {
//                            DropdownMenuItem(
//                                text = {
//                                    Text(
//                                        "Список пуст. Нажмите «Проверить подключение к Ollama»",
//                                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                                    )
//                                },
//                                onClick = { isModelDropdownExpanded = false },
//                                enabled = false
//                            )
//                        }
//                    }
//                }
//            }

                    var menuOpen by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = if (selectedModel.isBlank()) "Выберите модель" else selectedModel,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Выберите модель") },
                        trailingIcon = {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Открыть список"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (ollamaModels.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Список пуст. Нажмите «Проверить подключение к Ollama»",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = { menuOpen = false },
                                enabled = false
                            )
                        } else {
                            ollamaModels.forEach { modelName ->
                                DropdownMenuItem(
                                    text = { Text(modelName) },
                                    onClick = {
                                        selectedModel = modelName
                                        UserSession.selectedModel = modelName
                                        menuOpen = false
                                    }
                                )
                            }
                        }
                    }


                    // ------- КНОПКИ НИЗА ЭКРАНА -------
            Spacer(Modifier.height(8.dp))

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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (isClearingChats) "Очистка..." else "Очистить все чаты")
            }

            Button(
                onClick = {
                    viewModel.exportAllChats(context) { success ->
                        // Можешь что-то сделать после экспорта, если нужно
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0096FF)
                )
            ) {
                Text("Выгрузить все чаты в JSON", color = Color.White)
            }


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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isLoading)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(if (isLoading) "Сохранение..." else "Сохранить")
            }

            TextButton(
                onClick = { navController.navigate(Routes.Help) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "О приложении",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}}}
