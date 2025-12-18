package com.example.easybot.featureauth.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easybot.core.session.UserSession
import com.example.easybot.screens.navigation.Routes
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import com.example.easybot.featureauth.vm.RegistrationpageVM


@Composable
fun RegistrationPage(
    nav: NavController,
    vm: RegistrationpageVM = viewModel()
) {
    val context = LocalContext.current
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип
//            Image(
//                painter = painterResource(id = R.drawable.logo),
//                contentDescription = "App Logo",
//                modifier = Modifier.size(120.dp)
//            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Авторизация",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )


            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = vm.login,
                onValueChange = { vm.login = it },
                label = { Text("Логин") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = vm.password,
                onValueChange = { vm.password = it },
                label = { Text("Пароль") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp)
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

            Spacer(modifier = Modifier.height(22.dp))

            // Кнопка входа
            Button(
                onClick = {
                    vm.signIn { user ->

                        // 🔥 Уведомление про автосмену модели
                        if (user.modelChanged) {
                            Toast.makeText(
                                context,
                                "Модель поменялась автоматически на актуальную: ${user.model ?: "неизвестно"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        UserSession.userId = user.id?.toLong()
                        UserSession.login = user.login
                        nav.navigate(Routes.ChatList) {
                            popUpTo(Routes.Register) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "Вход", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    vm.signUp { user ->

                        if (user.modelChanged) {
                            Toast.makeText(
                                context,
                                "Модель поменялась автоматически на актуальную: ${user.model ?: "неизвестно"}",
                                Toast.LENGTH_LONG,
                            ).show()
                        }

                        UserSession.userId = user.id?.toLong()
                        UserSession.login = user.login
                        nav.navigate(Routes.ChatList) {
                            popUpTo(Routes.Register) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Регистрация",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
