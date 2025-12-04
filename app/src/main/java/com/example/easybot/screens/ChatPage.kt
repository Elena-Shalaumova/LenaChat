
package com.example.easybot.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easybot.R
import com.example.easybot.screens.theme.MessageModel
import com.example.easybot.screens.theme.ModelMessageGrey
import com.example.easybot.screens.theme.UserMessageBlue
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.compose.AsyncImage
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import com.example.easybot.SettingsRequest
import com.example.easybot.UserSession
import com.example.easybot.api
import kotlinx.coroutines.delay
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.text.input.KeyboardType
import com.example.easybot.ChatSettingsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


private fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) {
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ChatPage(
    chatId: Long,
    chatTitle: String,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val userId = UserSession.userId?.toInt()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var streamEnabled by remember { mutableStateOf(false) }

    // выбранная модель на время сессии
    var selectedModel by remember {
        mutableStateOf(UserSession.selectedModel ?: "")
    }

    // список моделей с бэка
    var oLlamaModels by remember { mutableStateOf<List<String>>(emptyList()) }

    var temperature by remember { mutableStateOf(UserSession.temperature ?: 0.7) }
    var maxTokens by remember { mutableStateOf(UserSession.maxTokens ?: 1024) }

    fun saveCurrentChatSettings() {
        saveChatSettingsFromChat(
            chatId = chatId,
            selectedModel = selectedModel,
            temperature = temperature,
            maxTokens = maxTokens,
            context = context,
            coroutineScope = coroutineScope
        )
    }

    // ---------- ШАГ 2: та самая функция сохранения из чата ----------
    fun saveSettingsFromChat() {
        val id = userId ?: return

        coroutineScope.launch {
            isLoading = true
            try {
                val response = api.saveChatSettings(
                    ChatSettingsRequest(
                        chatId = chatId.toInt(),
                        model = selectedModel,
                        temperature = temperature,
                        maxTokens = maxTokens
                    )
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Настройки чата сохранены",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Не удалось сохранить настройки чата",
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
    }


    // при входе в экран инициализируем чат + грузим модели/настройки
    LaunchedEffect(chatId) {
        viewModel.init(chatId)

        val id = userId ?: return@LaunchedEffect   // если null – просто ничего не делаем

        // 1. Модели
        try {
            val models = api.getAvailableModels()
            oLlamaModels = models
        } catch (e: Exception) {
            e.printStackTrace()
            oLlamaModels = emptyList()
        }

        // 2. Настройки КОНКРЕТНОГО чата
        try {
            val chatSettings = api.getChatSettings(chatId.toInt())

            selectedModel = chatSettings.model
            temperature   = chatSettings.temperature ?: 0.7
            maxTokens     = chatSettings.maxTokens ?: 1024

            UserSession.selectedModel = selectedModel
            UserSession.temperature   = temperature
            UserSession.maxTokens     = maxTokens

        } catch (e: retrofit2.HttpException) {
            if (e.code() == 404) {
                // записей в settings_chat ещё нет — берём глобальные
                val userSettings = api.getSettings(id)

                selectedModel = userSettings.model ?: oLlamaModels.firstOrNull().orEmpty()
                temperature   = userSettings.temperature ?: 0.7
                maxTokens     = userSettings.maxTokens ?: 1024

                UserSession.selectedModel = selectedModel
                UserSession.temperature   = temperature
                UserSession.maxTokens     = maxTokens

                // сразу создаём запись в settings_chat
                try {
                    api.saveChatSettings(
                        ChatSettingsRequest(
                            chatId = chatId.toInt(),
                            model = selectedModel,
                            temperature = temperature,
                            maxTokens = maxTokens
                        )
                    )
                } catch (saveEx: Exception) {
                    saveEx.printStackTrace()
                }
            } else {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val messages by viewModel.messages.collectAsState()
    val isAiBusy by viewModel.isAiBusy.collectAsState()

    // ---- прикрепленные картинки ----
    var pendingImagesBase64 by remember { mutableStateOf<List<String>>(emptyList()) }

    // дальше твои лаунчеры, MessageInput, список сообщений и т.д.
    // Я оставляю всё, как у тебя было, только AppHeader меняем ниже

    // ---------- ЛАУНЧЕР ОДНОЙ КАРТИНКИ ----------
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { pickedUri ->
                try {
                    val bytes = context.contentResolver
                        .openInputStream(pickedUri)
                        ?.use { it.readBytes() }
                        ?: return@rememberLauncherForActivityResult

                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    pendingImagesBase64 = listOf(base64)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    // ---------- ЛАУНЧЕР КАМЕРЫ ----------
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                val base64 = bitmapToBase64(bitmap)
                pendingImagesBase64 = pendingImagesBase64 + base64
            }
        }

    // ---------- ЛАУНЧЕР МУЛЬТИ-ГАЛЕРЕИ ----------
    val multiplePhotoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            if (uris.isNullOrEmpty()) return@rememberLauncherForActivityResult

            val imagesBase64 = uris.mapNotNull { uri ->
                uriToBase64(context, uri)
            }

            pendingImagesBase64 = pendingImagesBase64 + imagesBase64
        }

    // ---------- ЛАУНЧЕР РАЗРЕШЕНИЯ НА КАМЕРУ ----------
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraLauncher.launch(null)
            }
        }

    Column(modifier = modifier.fillMaxSize()) {

        // 🔼 ТУТ НОВЫЙ ВЫЗОВ AppHeader С ВЫБОРОМ МОДЕЛИ 🔼
        AppHeader(
            title = chatTitle,
            models = oLlamaModels,
            selectedModel = selectedModel,
            temperature = temperature,
            maxTokens = maxTokens,

            onModelSelected = { model ->
                selectedModel = model
                UserSession.selectedModel = model
                // сохраняем настройки ЧАТА
                saveCurrentChatSettings()
            },

            onTemperatureChange = { newTemp ->
                temperature = newTemp
                UserSession.temperature = newTemp
                saveCurrentChatSettings()
            },

            onMaxTokensChange = { tokens ->
                maxTokens = tokens
                UserSession.maxTokens = tokens
                saveCurrentChatSettings()
            },

            onClear = { viewModel.clearCurrentChat() },
            onSaveSettings = { saveSettingsFromChat() } // из слайдеров/поля ввода
        )

        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                EmptyChatScreen(modifier = Modifier.fillMaxSize())
            } else {
                MessageList(
                    modifier = Modifier.fillMaxSize(),
                    messageList = messages
                )
            }
        }

        // ---------- 🔥 НИЖНЯЯ ПАНЕЛЬ ВВОДА ----------
        MessageInput(
            hasAttachment = pendingImagesBase64.isNotEmpty(),
            isAiBusy = isAiBusy,
            onMessageSend = { text ->
                val imgs = pendingImagesBase64

                when {
                    imgs.size > 1 -> {
                        viewModel.sendImagesMessage(
                            images = imgs,
                            prompt = text.ifBlank { null }
                        )
                    }
                    imgs.size == 1 -> {
                        viewModel.sendImageMessage(
                            base64Image = imgs.first(),
                            prompt = text.ifBlank { null }
                        )
                    }
                    else -> {
                        viewModel.sendMessage(text)
                    }
                }

                pendingImagesBase64 = emptyList()
            },
            onPickImage = { imagePickerLauncher.launch("image/*") },
            onPickMultipleImages = {
                multiplePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCaptureImage = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) cameraLauncher.launch(null)
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onClearAttachment = { pendingImagesBase64 = emptyList() }
        )
    }
}


@Composable
fun EmptyChatScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.size(60.dp),
            painter = painterResource(id = R.drawable.baseline_question_answer_24),
            contentDescription = "Icon",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Text(
            text = "Ask me anything",
            fontSize = 22.sp,
            color = Color.Gray)
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>
) {
    // состояние скролла списка
    val listState = rememberLazyListState()

    // когда добавилось новое сообщение — скроллимся в самый низ
    LaunchedEffect(messageList.size) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(messageList.lastIndex)
        }
    }

    // когда меняется текст последнего сообщения (стриминг) — тоже докручиваем вниз
    val lastMessage = messageList.lastOrNull()
    LaunchedEffect(lastMessage?.id, lastMessage?.text) {
        if (messageList.isNotEmpty()) {
            listState.animateScrollToItem(messageList.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        reverseLayout = false,         // обычный порядок
        state = listState
    ) {
        items(
            items = messageList,
            key = { msg -> "${msg.id}_${msg.createdAt}" }
        ) { msg ->
            MessageBubble(message = msg)
        }
    }
}

@Composable
fun MessageBubble(message: MessageModel) {
    val isUserMessage = message.role == 1   // 1 – пользователь, 0 – модель

    // считаем, что это "пишущий" бот:
    // плейсхолдер, который ты добавляешь с id = -2L и text = "..."
    val isTypingPlaceholder = (message.id == -2L && message.text == "...")

    // локальный текст, который реально рисуем в пузырьке
    var visibleText by remember(message.id, message.text) {
        mutableStateOf(message.text.orEmpty())
    }

    // анимация "..." → "......" → "........."
    LaunchedEffect(isTypingPlaceholder, message.text) {
        if (!isTypingPlaceholder) {
            // как только в это сообщение прилетел нормальный текст – просто показываем его
            visibleText = message.text.orEmpty()
            return@LaunchedEffect
        }

        val frames = listOf("...", "......", ".........")

        while (true) {
            for (frame in frames) {
                visibleText = frame
                delay(350L)   // скорость мигания
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isUserMessage) UserMessageBlue else ModelMessageGrey)
                .padding(12.dp)
        ) {

            Column {
                // ----- ТЕКСТ -----
                if (visibleText.isNotBlank()) {
                    SelectionContainer {
                        Text(
                            text = visibleText,
                            fontWeight = FontWeight.W500,
                            color = Color.Black
                        )
                    }
                }

                // ---------- СПИСОК КАРТИНОК ----------
                if (message.images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(message.images) { base64 ->
                            val bitmap = remember(base64) {
                                try {
                                    val clean = base64.substringAfter("base64,", base64)
                                    val bytes = Base64.decode(clean, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (e: IllegalArgumentException) {
                                    null
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = message.text ?: "image",
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = "❌ не удалось загрузить картинку",
                                    color = Color.Red,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    hasAttachment: Boolean,
    isAiBusy: Boolean,
    onMessageSend: (String) -> Unit,
    onPickImage: () -> Unit,
    onPickMultipleImages: () -> Unit,
    onCaptureImage: () -> Unit,
    onClearAttachment: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    var showAttachments by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 40.dp, top = 8.dp)
    ) {

        // --- Индикатор, что есть прикреплённые картинки ---
        if (hasAttachment) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F8FE))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Вложено изображение",
                    color = Color(0xFF0D47A1),
                    fontSize = 12.sp
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onClearAttachment) {
                    Text(text = "Убрать", fontSize = 12.sp)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // КНОПКА-СКРЕПКА (открывает меню вложений)
                IconButton(onClick = { showAttachments = true }) {
                    Icon(
                        imageVector = Icons.Filled.AttachFile,
                        contentDescription = "Вложения",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // ПОЛЕ ВВОДА
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = message,
                    onValueChange = { message = it },

                    // 👉 Делаем поле многострочным
                    singleLine = false,
                    maxLines = 5, // сколько строк максимум показать

                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Default  // чтобы клавиатура не пыталась "отправить"
                    ),

                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE8F8FE),
                        unfocusedContainerColor = Color(0xFFE8F8FE),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )


                // КНОПКА ОТПРАВКИ
                val isSendEnabled = !isAiBusy && (message.isNotEmpty() || hasAttachment)
                IconButton(
                    onClick = {
                        if (message.isNotEmpty() || hasAttachment) {
                            onMessageSend(message)
                            message = ""
                        }
                    },
                      //enabled = !isAiBusy && (message.isNotEmpty() || hasAttachment)
                    enabled = isSendEnabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        //tint = MaterialTheme.colorScheme.primary
                        tint = if (isSendEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray
                        }
                    )
                }
            }

            // ВЫПАДАЮЩЕЕ МЕНЮ ВЛОЖЕНИЙ
            DropdownMenu(
                expanded = showAttachments,
                onDismissRequest = { showAttachments = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Сделать фото") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showAttachments = false
                        onCaptureImage()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Выбрать одно фото") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Выбрать одно фото"
                        )
                    },
                    onClick = {
                        showAttachments = false
                        onPickImage()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Выбрать несколько фото") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Выбрать несколько фото"
                        )
                    },
                    onClick = {
                        showAttachments = false
                        onPickMultipleImages()
                    }
                )
            }
        }
    }
}

/** Bitmap -> base64 */
private fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    val bytes = stream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

private fun saveChatSettingsFromChat(
    chatId: Long,
    selectedModel: String,
    temperature: Double,
    maxTokens: Int,
    context: Context,
    coroutineScope: CoroutineScope
) {
    if (selectedModel.isBlank()) return   // пустую модель не шлём

    coroutineScope.launch {
        try {
            val response = api.saveChatSettings(
                request = ChatSettingsRequest(
                    chatId = chatId.toInt(),
                    model = selectedModel,
                    temperature = temperature,
                    maxTokens = maxTokens
                )
            )

            if (!response.isSuccessful) {
                Toast.makeText(
                    context,
                    "Не удалось сохранить настройки чата",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Ошибка: ${e.message ?: "неизвестная"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun AppHeader(
    title: String,
    models: List<String>,
    selectedModel: String,
    temperature: Double,
    maxTokens: Int,
    onModelSelected: (String) -> Unit,
    onTemperatureChange: (Double) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
    onClear: () -> Unit = {},
    onSaveSettings: () -> Unit = {}      // 👈 добавили новый колбэк
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {

        // ---------- Верхняя строка: название чата + "Очистить" ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onClear) {
                Text(
                    text = "Очистить",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }

        if (models.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))

            // ---------- Выпадающее меню моделей + ползунки ----------
            ModelSelector(
                models = models,
                selectedModel = selectedModel,
                onModelSelected = onModelSelected,
                modifier = Modifier.fillMaxWidth(),
                extraContent = {
                    // 👉 сюда кладём температуру и maxTokens

                    var showTempInfo by remember { mutableStateOf(false) }
                    var showTokensInfo by remember { mutableStateOf(false) }

                    // --- Заголовок температуры ---
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Температура: ${"%.2f".format(temperature)}",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showTempInfo = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Что такое температура?"
                            )
                        }
                    }

                    // --- Slider температуры ---
                    Slider(
                        value = temperature.toFloat(),
                        onValueChange = { value ->
                            onTemperatureChange(value.toDouble())
                        },
                        onValueChangeFinished = {
                            // палец отпустили → сохраняем настройки чата
                            onSaveSettings()
                        },
                        valueRange = 0f..2f
                    )

                    Spacer(Modifier.height(12.dp))

                    // ===== МАКСИМАЛЬНАЯ ДЛИНА (ТОКЕНЫ) + ? =====
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Максимальная длина ответа (токены)",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showTokensInfo = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Что такое максимальная длина?"
                            )
                        }
                    }

                    // текст в поле синхронизируем с maxTokens
                    var tokensText by remember(maxTokens) {
                        mutableStateOf(maxTokens.toString())
                    }

                    OutlinedTextField(
                        value = tokensText,
                        onValueChange = { text ->
                            val digits = text.filter { it.isDigit() }
                            tokensText = digits

                            if (digits.isNotEmpty()) {
                                val valueInt = digits.toInt().coerceIn(64, 4096)
                                onMaxTokensChange(valueInt)
                                onSaveSettings()              // сохраняем при изменении
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        label = {
                            Text(text = "Например: 512, 1024, 2048")
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // ===== Диалог для температуры =====
                    if (showTempInfo) {
                        AlertDialog(
                            onDismissRequest = { showTempInfo = false },
                            title = { Text("Температура") },
                            text = {
                                Text(
                                    "0 — консервативные и предсказуемые ответы\n" +
                                            "2 — креативные и непредсказуемые ответы"
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { showTempInfo = false }) {
                                    Text("Понятно")
                                }
                            }
                        )
                    }

                    // ===== Диалог для maxTokens =====
                    if (showTokensInfo) {
                        AlertDialog(
                            onDismissRequest = { showTokensInfo = false },
                            title = { Text("Максимальная длина ответа") },
                            text = {
                                Text(
                                    "Ограничивает максимальное количество токенов " +
                                            "в одном ответе модели. Меньше — короче и быстрее, " +
                                            "больше — длиннее и детальнее."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { showTokensInfo = false }) {
                                    Text("Понятно")
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ===== Кнопка сброса к стандартным значениям =====
                    Button(
                        onClick = {
                            onTemperatureChange(1.0)
                            onMaxTokensChange(1000)
                            onSaveSettings()       // 👈 тоже сохраняем
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Сбросить параметры к стандартным")
                    }
                })}}}
