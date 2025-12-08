package com.example.easybot.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    models: List<String>,
    selectedModel: String?,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    // 🔽 дополнительный контент под списком моделей (температура, maxTokens)
    extraContent: @Composable ColumnScope.() -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded },
        modifier = modifier
    ) {
        TextField(
            value = selectedModel ?: "",
            onValueChange = { },             // только выбор из списка
            readOnly = true,

            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            // --- список моделей ---
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelSelected(model)
                        isExpanded = false
                    }
                )
            }

            // 🔵 разделитель + область под ползунки
            Divider(
                color = MaterialTheme.colorScheme.primary,
                thickness = 1.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Spacer(Modifier.height(8.dp))
                extraContent()   // <--- сюда ты потом передашь температуру и maxTokens
            }
        }
    }
}
