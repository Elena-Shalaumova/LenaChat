package com.example.easybot.screens.theme

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
fun SignUpPage(navController: NavController) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Registration", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = login,
            onValueChange = { login = it; error = null },
            label = { Text("Login or Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPass = !showPass }) {
                    Text(if (showPass) "Hide" else "Show")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; error = null },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showConfirm = !showConfirm }) {
                    Text(if (showConfirm) "Hide" else "Show")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

//        Button(
//            onClick = {
//                when {
//                    login.isBlank() || password.isBlank() || confirm.isBlank() ->
//                        error = "Заполните все поля"
//                    password != confirm ->
//                        error = "Пароли не совпадают"
//                    else -> {
//                        val encoded = Uri.encode(login)
//                        navController.navigate("${Routes.Chat}/$encoded") {
//                            popUpTo(Routes.SignIn) { inclusive = true }
//                        }
//                    }
//                }
//            },
//            enabled = login.isNotBlank() && password.isNotBlank() && confirm.isNotBlank(),
//            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
//        ) {
//            Text("Sign up")
//        }

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("У меня уже есть аккаунт — Войти")
        }
    }
}
