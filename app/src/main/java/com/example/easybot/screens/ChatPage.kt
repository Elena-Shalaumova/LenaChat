package com.example.easybot.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.easybot.MessageDto
import com.example.easybot.R
import com.example.easybot.screens.theme.ModelMessageGrey
import com.example.easybot.screens.theme.UserMessageBlue

@Composable
fun ChatPage(
    chatId: Long,
    chatTitle: String,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    LaunchedEffect(chatId) {
        viewModel.init(chatId)
    }

    val messages by viewModel.messages.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        AppHeader(title = chatTitle, onClear = { viewModel.clearCurrentChat() })

        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                EmptyChatScreen(modifier = Modifier.fillMaxSize())
            } else {
                MessageList(modifier = Modifier.fillMaxSize(), messageList = messages)
            }
        }

        MessageInput(onMessageSend = { viewModel.sendMessage(it) })
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
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageDto>) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        reverseLayout = true
    ) {
        items(messageList.reversed(), key = { it.id }) {
            MessageRow(messageDto = it)
        }
    }
}

@Composable
fun MessageRow(messageDto: MessageDto) {
    // Роль 1 - пользователь, 0 - модель
    val isUserMessage = messageDto.role == 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isUserMessage) UserMessageBlue else ModelMessageGrey)
                .padding(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = messageDto.text,
                    fontWeight = FontWeight.W500,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 40.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        IconButton(onClick = {
            if (message.isNotEmpty()) {
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(
                imageVector = Icons.Default.Send,
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
