package com.example.easybot.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.easybot.MessageModel
import com.example.easybot.R
import com.example.easybot.screens.theme.ColorModelMessage
import com.example.easybot.screens.theme.ColorUserMessage
import com.example.easybot.screens.theme.Purple80


@Composable
fun ChatPage(
    chatId: Long,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    LaunchedEffect(chatId) {
        viewModel.init(chatId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppHeader(onClear = { viewModel.clearCurrentChat() })

        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList
        )

        MessageInput(onMessageSend = {
            viewModel.sendMessage(it)
        })
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(messageList.reversed()) {
                MessageRow(messageModel = it)
            }
        }
    }
}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isModel) ColorModelMessage else ColorUserMessage)
                .padding(16.dp)
        ) {
            SelectionContainer {
                Text(
                    text = messageModel.message,
                    fontWeight = FontWeight.W500,
                    color = Color.Black // <-- Шрифт в сообщениях сделал черным
                )
            }
        }
    }
}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 40.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = {
                message = it
            },
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
fun AppHeader(onClear: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Alabuga AI Bot",
            color = Color.Black, // <-- Шрифт в шапке сделал черным
            fontSize = 26.sp,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onClear) {
            Text(
                text = "Очистить",
                color = Color.Black, // <-- Шрифт "Очистить" сделал черным
                fontSize = 16.sp
            )
        }
    }
}