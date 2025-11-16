package com.example.easybot.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easybot.navigation.Routes
import com.example.easybot.UserSession

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
        Text(
            text = "Authorization",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = vm.login,
            onValueChange = { vm.login = it },
            label = { Text("Login") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                    UserSession.userId = user.id?.toLong()
                    UserSession.login = user.login
                    nav.navigate(Routes.ChatList) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Sign in", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Кнопка регистрации
        TextButton(
            onClick = {
                vm.signUp { user -> // <-- ВОТ ЗДЕСЬ Я ИСПРАВИЛ signIn на signUp
                    UserSession.userId = user.id?.toLong()
                    UserSession.login = user.login
                    nav.navigate(Routes.ChatList) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
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