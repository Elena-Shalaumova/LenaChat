package com.example.easybot.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.easybot.navigation.Routes

@Composable
fun RegistrationPage(navController: NavController) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("authorization", style = MaterialTheme.typography.headlineMedium,     color = MaterialTheme.colorScheme.primary)

        OutlinedTextField(
            value = login,
            onValueChange = { login = it; error = null },
            label = { Text("Login or Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = {
                when {
                    login.isBlank() || password.isBlank() ->
                        error = "Введите логин и пароль"
                    login == "test" && password == "123456" -> {
                        // Передаём логин на следующий экран (кодируем на всякий случай)
                        val encoded = Uri.encode(login)
                        navController.navigate("${Routes.Chat}/$encoded")
                    }
                    else -> error = "Неверный логин или пароль"
                }
            },
            enabled = login.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Sign in")
        }
    }
}
