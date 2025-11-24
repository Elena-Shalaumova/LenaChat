package com.example.easybot.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easybot.R
import com.example.easybot.screens.theme.ModelMessageGrey
import com.example.easybot.screens.theme.UserMessageBlue
import com.example.easybot.screens.theme.MessageModel   // UI-модель сообщения

@Composable
fun ChatPage(
    chatId: Long,
    chatTitle: String,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    // инициализация чата
    LaunchedEffect(chatId) {
        viewModel.init(chatId)
    }

    val messages by viewModel.messages.collectAsState()
    val context = LocalContext.current

    // лаунчер выбора картинки из галереи
    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { pickedUri ->
                try {
                    val bytes = context.contentResolver
                        .openInputStream(pickedUri)
                        ?.use { it.readBytes() }
                        ?: return@rememberLauncherForActivityResult

                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                    // отправляем картинку во ViewModel
                    viewModel.sendImageMessage(
                        base64Image = base64,
                        prompt = null // позже можно сделать отдельное поле "подпись к картинке"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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

        MessageInput(
            onMessageSend = { text -> viewModel.sendMessage(text) },
            onPickImage = { imagePickerLauncher.launch("image/*") }
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
        // отрисовка сообщений (текст + картинки)
        items(messageList.reversed(), key = { it.id }) { msg ->
            MessageBubble(message = msg)
        }
    }
}

@Composable
fun MessageBubble(message: MessageModel) {
    // 1 – пользователь, 0 – модель
    val isUserMessage = message.role == 1

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
            // ==== КАРТИНКА ====
            if (message.type == "image" && !message.imageBase64.isNullOrEmpty()) {

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

            } else {
                // ==== ТЕКСТ ====
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
    onMessageSend: (String) -> Unit,
    onPickImage: () -> Unit
) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 40.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- КНОПКА КАРТИНКИ ---
        IconButton(onClick = { onPickImage() }) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = "Pick Image",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        // --- ТЕКСТОВОЕ ПОЛЕ ---
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

        // --- ОТПРАВКА ТЕКСТА ---
        IconButton(
            onClick = {
                if (message.isNotEmpty()) {
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
