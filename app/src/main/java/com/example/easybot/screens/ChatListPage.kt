package com.example.easybot.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easybot.App
import com.example.easybot.Routes

@Composable
fun ChatListPage(nav: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as App

    // ВАЖНО: тут берем app.chatRepo, а не app.repo()
    val vm: ChatListViewModel = viewModel(
        factory = ChatListVmFactory(app.chatRepo)
    )

    val state by vm.state.collectAsState()

    var newTitle by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val title = if (newTitle.isBlank()) "Новый чат" else newTitle
                    vm.createChat(title)
                    newTitle = ""
                }
            ) {
                Text("Новый чат")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Название чата") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn {
                items(state.items, key = { it.id }) { chat ->
                    ListItem(
                        headlineContent = { Text(chat.title) },
                        supportingContent = { Text("обновлён: ${chat.updatedAt}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                nav.navigate("${Routes.ChatPage}/${chat.id}")
                            }
                            .padding(vertical = 4.dp),
                        trailingContent = {
                            IconButton(onClick = { vm.deleteChat(chat.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Удалить"
                                )
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
