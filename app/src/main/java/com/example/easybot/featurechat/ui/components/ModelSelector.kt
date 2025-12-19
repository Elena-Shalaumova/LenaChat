package com.example.easybot.featurechat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    models: List<String>,
    selectedModel: String?,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    includeExtraContent: Boolean = false,
    // если вдруг ещё где-то используешь
    extraContent: @Composable ColumnScope.() -> Unit = {},
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
                .fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),

            colors = TextFieldDefaults.colors(

                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,

                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,

                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            shape = RoundedCornerShape(12.dp)
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelSelected(model)
                        isExpanded = false
                    }
                )
            }

            if (includeExtraContent) {
                Divider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 1.dp
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(1.dp))
                    extraContent()
                }
            }
        }
    }
}
