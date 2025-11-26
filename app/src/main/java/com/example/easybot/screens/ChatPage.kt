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

    // ---- тут храним "прикреплённую" картинку для следующего сообщения ----
    var pendingImageBase64 by remember { mutableStateOf<String?>(null) }

    // ---------- ЛАУНЧЕР ГАЛЕРЕИ ----------
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { pickedUri ->
                try {
                    val bytes = context.contentResolver
                        .openInputStream(pickedUri)
                        ?.use { it.readBytes() }
                        ?: return@rememberLauncherForActivityResult

                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                    // НЕ отправляем сразу, а прикрепляем к следующему сообщению
                    pendingImageBase64 = base64
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    // ---------- ЛАУНЧЕР КАМЕРЫ ----------
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let { captured ->
                // Bitmap → base64 → тоже просто прикрепляем
                pendingImageBase64 = bitmapToBase64(captured)
            }
        }

    // ---------- ЛАУНЧЕР ЗАПРОСА РАЗРЕШЕНИЯ НА КАМЕРУ ----------
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

        // ---------- НИЖНЯЯ ПАНЕЛЬ ВВОДА ----------
        MessageInput(
            hasAttachment = pendingImageBase64 != null,
            onMessageSend = { text ->
                val img = pendingImageBase64

                if (img != null) {
                    // Есть прикреплённая картинка -> отправляем и текст, и картинку
                    viewModel.sendImageMessage(
                        base64Image = img,
                        prompt = text.ifBlank { null }  // текст-вопрос к картинке
                    )
                    pendingImageBase64 = null
                } else {
                    // Картинки нет -> обычное текстовое сообщение
                    viewModel.sendMessage(text)
                }
            },
            onPickImage = { imagePickerLauncher.launch("image/*") },
            onCaptureImage = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    cameraLauncher.launch(null)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onClearAttachment = { pendingImageBase64 = null }
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
        Text(text = "Ask me anything", fontSize = 22.sp, color = Color.Gray)
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        reverseLayout = true
    ) {
        items(messageList.reversed(), key = { it.id }) { msg ->
            MessageBubble(message = msg)
        }
    }
}

@Composable
fun MessageBubble(message: MessageModel) {
    val isUserMessage = message.role == 1   // 1 – пользователь, 0 – модель

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
                .padding(16.dp)
        ) {
            if (message.type == "image" && !message.imageBase64.isNullOrEmpty()) {
                // ===== КАРТИНКА + ТЕКСТ ПОД НЕЙ =====
                Column {
                    // --- картинка ---
                    val bytes = remember(message.imageBase64) {
                        try {
                            Base64.decode(message.imageBase64, Base64.DEFAULT)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }

                    if (bytes != null) {
                        val bitmap = remember(bytes) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = message.text ?: "image message",
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        } else {
                            Text(
                                text = "Не удалось отрисовать картинку",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Некорректные данные изображения",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    // --- подпись под картинкой, если текст не пустой ---
                    if (!message.text.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = message.text,
                                fontWeight = FontWeight.W500,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // ===== ТОЛЬКО ТЕКСТ =====
                SelectionContainer {
                    Text(
                        text = message.text ?: "",
                        fontWeight = FontWeight.W500,
                        color = Color.Black
                    )
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
        // Индикатор, что есть прикреплённая картинка
        if (hasAttachment) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE8F5FE))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Вложено изображение", color = Color(0xFF0D47A1), fontSize = 12.sp)
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onClearAttachment) {
                    Text("Убрать", fontSize = 12.sp)
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
                // ОДНА КНОПКА-СКРЕПКА
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
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFE8F5FE),
                        unfocusedContainerColor = Color(0xFFE8F5FE),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // ОТПРАВКА ТЕКСТА (+ прицепленная картинка, если есть)
                IconButton(
                    onClick = {
                        if (message.isNotEmpty() || hasAttachment) {
                            onMessageSend(message)
                            message = ""
                        }
                    }
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
                    text = { Text("Выбрать из галереи") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        showAttachments = false
                        onPickImage()
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
