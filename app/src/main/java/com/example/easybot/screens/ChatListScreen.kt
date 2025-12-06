package com.example.easybot.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.easybot.ChatDto
import com.example.easybot.ChatListViewModel
import com.example.easybot.navigation.Routes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import com.example.easybot.ChatListItem

@Composable
fun ChatListScreen(
    navController: NavHostController,
    viewModel: ChatListViewModel = viewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // диалог создания
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var newChatTitle by remember { mutableStateOf("") }
    // ⚡ флаг "инкогнито чат"
    var isIncognito by remember { mutableStateOf(false) }

    // диалог переименования
    var isRenameDialogOpen by remember { mutableStateOf(false) }
    var renameChatTitle by remember { mutableStateOf("") }
    var renameChatId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadChats()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color(0xFFE8F5FE),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Мои чаты",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { navController.navigate(Routes.Settings) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newChatTitle = ""
                    isIncognito = false          // по умолчанию обычный чат
                    isCreateDialogOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать новый чат",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->

        // ---- Диалог "Новый чат" ----
        if (isCreateDialogOpen) {
            AlertDialog(
                onDismissRequest = { isCreateDialogOpen = false },
                title = { Text("Новый чат") },
                text = {
                    Column {
                        Text("Укажите название чата")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newChatTitle,
                            onValueChange = { newChatTitle = it },
                            label = { Text("Название чата") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🔹 переключатель "Инкогнито чат"
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Инкогнито чат",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isIncognito,
                                onCheckedChange = { isIncognito = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val title = newChatTitle.trim()
                            if (title.isNotEmpty()) {
                                // передаём флаг инкогнито во ViewModel
                                viewModel.createChat(title, isIncognito = isIncognito)
                                isCreateDialogOpen = false
                            }
                        },
                        enabled = newChatTitle.isNotBlank()
                    ) {
                        Text("Создать")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isCreateDialogOpen = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        // ---- Диалог "Переименовать чат" ----
        if (isRenameDialogOpen && renameChatId != null) {
            AlertDialog(
                onDismissRequest = { isRenameDialogOpen = false },
                title = { Text("Переименовать чат") },
                text = {
                    Column {
                        Text("Новое название чата")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = renameChatTitle,
                            onValueChange = { renameChatTitle = it },
                            label = { Text("Название чата") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val title = renameChatTitle.trim()
                            val id = renameChatId
                            if (id != null && title.isNotEmpty()) {
                                viewModel.renameChat(id, title)
                                isRenameDialogOpen = false
                            }
                        },
                        enabled = renameChatTitle.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isRenameDialogOpen = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пока нет чатов.\nНажми +, чтобы создать первый 👇",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = chats, key = { it.chatId }) { chat ->
                    ChatRow(
                        chat = chat,
                        onOpen = {
                            val encodedTitle = URLEncoder.encode(
                                chat.title,
                                StandardCharsets.UTF_8.toString()
                            )

                            val incognitoFlag = if (chat.isIncognito) 1 else 0

                            navController.navigate("chat/${chat.chatId}/$encodedTitle/$incognitoFlag")
                        },

                                onRename = {
                            renameChatId = chat.chatId
                            renameChatTitle = chat.title
                            isRenameDialogOpen = true
                        },
                        onDelete = {
                            viewModel.deleteChat(chat.chatId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatListItem,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Верхняя строка: название чата + иконки
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // время последнего сообщения
                chat.lastMessageTime?.let { time ->
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCCE2FF)
                    )
                }

                IconButton(onClick = onRename) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Переименовать чат",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить чат",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // последнее сообщение
            if (chat.lastMessageText.isNotBlank()) {
                Text(
                    text = chat.lastMessageText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Нижняя строка: модель
            val modelText = chat.modelName
                ?.takeIf { it.isNotBlank() }
                ?: "Модель не выбрана"

            Text(
                text = modelText,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCCE2FF)
            )
        }
    }
}
