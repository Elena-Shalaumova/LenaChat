package com.example.easybot.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.easybot.ChatListItem

@Composable
fun ChatRow(
    chat: ChatListItem,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val editInteractionSource = remember { MutableInteractionSource() }
    val isEditHovered by editInteractionSource.collectIsHoveredAsState()
    val editIconColor = if (isEditHovered) MaterialTheme.colorScheme.primary else Color.White

    val deleteInteractionSource = remember { MutableInteractionSource() }
    val isDeleteHovered by deleteInteractionSource.collectIsHoveredAsState()
    val deleteIconColor = if (isDeleteHovered) MaterialTheme.colorScheme.primary else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                chat.lastMessageTime?.let { time ->
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = onRename,
                    interactionSource = editInteractionSource,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = editIconColor)
                )  {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Переименовать чат",
                        tint = editIconColor
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить чат",
                        tint = deleteIconColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val modelText = chat.modelName
                ?.takeIf { it.isNotBlank() }
                ?: "Модель не выбрана"

            Text(
                text = modelText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}
