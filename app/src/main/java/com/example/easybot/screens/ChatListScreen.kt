package com.example.easybot.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.easybot.ChatListItem
import com.example.easybot.ChatListViewModel
import com.example.easybot.components.ChatRow
import com.example.easybot.navigation.Routes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.isSystemInDarkTheme
@Composable
fun ChatListScreen(
    navController: NavHostController,
    viewModel: ChatListViewModel = viewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()

    // диалог создания
    var isCreateDialogOpen by remember { mutableStateOf(false) }
    var newChatTitle by remember { mutableStateOf("") }
    var isIncognito by remember { mutableStateOf(false) }

    // диалог переименования
    var isRenameDialogOpen by remember { mutableStateOf(false) }
    var renameChatTitle by remember { mutableStateOf("") }
    var renameChatId by remember { mutableStateOf<Int?>(null) }

    // перезагрузка чатов при возврате на экран
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
        containerColor = MaterialTheme.colorScheme.background,
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
                    //color = MaterialTheme.colorScheme.primary,
                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { navController.navigate(Routes.Settings) }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Настройки",
                        //tint = MaterialTheme.colorScheme.primary
                        tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.primary
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать новый чат",
                    tint = MaterialTheme.colorScheme.onPrimary,
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Инкогнито чат", modifier = Modifier.weight(1f))
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
                                coroutineScope.launch {
                                    // suspend-функция из VM
                                    val createdChat = viewModel.createChat(
                                        title = title,
                                        isIncognito = isIncognito
                                    )

                                    isCreateDialogOpen = false

                                    val encodedTitle = URLEncoder.encode(
                                        createdChat.title,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                    val flag = if (createdChat.isIncognito) 1 else 0

                                    navController.navigate(
                                        "chat/${createdChat.chatId}/$encodedTitle/$flag"
                                    )
                                }
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

                            navController.navigate(
                                "chat/${chat.chatId}/$encodedTitle/$incognitoFlag"
                            )
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

