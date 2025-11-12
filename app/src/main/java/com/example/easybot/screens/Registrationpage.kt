package com.example.easybot.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easybot.navigation.Routes



//@Composable
//fun RegistrationPage(navController: NavController) {
//    var login by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var showPassword by remember { mutableStateOf(false) }
//    var error by remember { mutableStateOf<String?>(null) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text("authorization", style = MaterialTheme.typography.headlineMedium,     color = MaterialTheme.colorScheme.primary)
//
//        OutlinedTextField(
//            value = login,
//            onValueChange = { login = it; error = null },
//            label = { Text("Login or Email") },
//            singleLine = true,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 16.dp)
//        )
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it; error = null },
//            label = { Text("Password") },
//            singleLine = true,
//            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
//            trailingIcon = {
//                TextButton(onClick = { showPassword = !showPassword }) {
//                    Text(if (showPassword) "Hide" else "Show")
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 12.dp)
//        )
//
//        error?.let {
//            Text(
//                text = it,
//                color = MaterialTheme.colorScheme.error,
//                modifier = Modifier.padding(top = 8.dp)
//            )
//        }
//
//        Button(
//            onClick = {
//                when {
//                    login.isBlank() || password.isBlank() ->
//                        error = "Введите логин и пароль"
//                    login == "test" && password == "123456" -> {
//                        // Передаём логин на следующий экран (кодируем на всякий случай)
//                        val encoded = Uri.encode(login)
//                        navController.navigate("${Routes.Chat}/$encoded")
//                    }
//                    else -> error = "Неверный логин или пароль"
//                }
//            },
//            enabled = login.isNotBlank() && password.isNotBlank(),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 16.dp)
//        ) {
//            Text("Sign in")
//        }
//        // ссылка на экран регистрации
////        TextButton(
////            onClick = { navController.navigate(Routes.SignUp) },
////            modifier = Modifier.padding(top = 8.dp)
////        ) { Text("Нет аккаунта? Создать") }
//    }
//}

@Composable
fun RegistrationPage(
    nav: NavController,
    vm: RegistrationpageVM = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Заголовок
        Text(
            text = "Authorization",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Поле логина
        OutlinedTextField(
            value = vm.login,
            onValueChange = { vm.login = it },
            label = { Text("Login") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Поле пароля
        OutlinedTextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ошибка (если есть)
        vm.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Кнопка входа
        Button(
            onClick = {
                vm.signIn { user ->
                    nav.navigate("chat/${Uri.encode(user.login)}")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Sign in", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка регистрации
        TextButton(
            onClick = {
                vm.signUp { user ->
                    nav.navigate("chat/${Uri.encode(user.login)}")
                }
            }
        ) {
            Text(
                text = "Sign up",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
