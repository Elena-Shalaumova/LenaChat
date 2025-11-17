package com.example.easybot.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.easybot.data.local.ChatEntity
import com.example.easybot.navigation.Routes
import com.example.easybot.screens.theme.ChatListViewModel

@Composable
fun ChatListScreen(
    navController: NavHostController,
    viewModel: ChatListViewModel = viewModel()
) {
    val chats: List<ChatEntity> by viewModel.chats.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color(0xFFE8F5FE),
        // Я убрал topBar отсюда, чтобы получить больше контроля
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val index = chats.size + 1
                    viewModel.createChat("Новый чат #$index")
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
    ) { paddingValues -> // Эти отступы теперь только для системных элементов (status bar, navigation bar)

        // Используем LazyColumn для всего контента, чтобы он был единым списком
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // Применяем системные отступы
                .padding(paddingValues),
            // Добавляем наши собственные отступы: 80.dp сверху, чтобы сдвинуть всё вниз
            contentPadding = PaddingValues(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Заголовок теперь - первый элемент списка
            item {
                Text(
                    text = "Мои чаты",
                    style = MaterialTheme.typography.headlineLarge, // Сделал стиль чуть больше
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp) // Отступ после заголовка
                )
            }

            if (chats.isEmpty()) {
                item {
                    // Пустое состояние
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth() // Занимаем всю ширину
                            .padding(vertical = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Пока нет чатов.\nНажми +, чтобы создать первый 👇",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Список чатов
                items(chats, key = { it.id }) { chat ->
                    ChatRow(
                        chat = chat,
                        onOpen = {
                            navController.navigate("chat/${chat.id}")
                        },
                        onDelete = {
                            viewModel.deleteChat(chat.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
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
    }
}
