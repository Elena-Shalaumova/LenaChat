
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
import kotlinx.coroutines.delay

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
    // при входе в экран инициализируем чат
    LaunchedEffect(chatId) {
        viewModel.init(chatId)
    }

    val messages by viewModel.messages.collectAsState()
    val context = LocalContext.current
    //val isAiBusy by viewModel.isAiBusy.collectAsState()


    // ---- прикрепленные картинки ----
    var pendingImagesBase64 by remember { mutableStateOf<List<String>>(emptyList()) }

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
                uriToBase64(context, uri)   // helper — см. ниже
            }

            // ДОБАВЛЯЕМ к уже прикреплённым
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

        AppHeader(title = chatTitle, onClear = { viewModel.clearCurrentChat() })


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

            // выбрать одно изображение
            onPickImage = {
                imagePickerLauncher.launch("image/*")
            },

            // выбрать несколько изображений
            onPickMultipleImages = {
                multiplePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },

            // камера
            onCaptureImage = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) cameraLauncher.launch(null)
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },

            // удалить прикрепленные
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
                IconButton(
                    onClick = {
                        // если ИИ занят – просто выходим
                        // if (isAiBusy) return@IconButton

                        if (message.isNotEmpty() || hasAttachment) {
                            onMessageSend(message)
                            message = ""
                        }
                    },
                    //  enabled = !isAiBusy && (message.isNotEmpty() || hasAttachment)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
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

@Composable
fun AppHeader(title: String, onClear: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
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
}
