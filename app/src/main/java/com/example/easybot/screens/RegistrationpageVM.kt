package com.example.easybot.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.LoginReq
import com.example.easybot.UserDto
import com.example.easybot.RegisterDto
import com.example.easybot.WebApiChatAI
import com.example.easybot.provideApi
import kotlinx.coroutines.launch

class RegistrationpageVM(private val api: WebApiChatAI = provideApi()
) : ViewModel() {
    var login by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)

    fun signIn(onSuccess: (UserDto) -> Unit) = viewModelScope.launch {
        error = null

        if (login.isBlank() || password.isBlank()) {
            error = "Введите логин и пароль"
            return@launch
        }

        loading = true
        try {
            val resp = api.login(req = LoginReq(login, password))

            if (resp.isSuccessful) {
                resp.body()?.let { user ->
                    onSuccess(user)
                } ?: run {
                    error = "Пустой ответ сервера"
                }

            } else {
                error = when (resp.code()) {
                    401 -> "Неверный логин/пароль"
                    404 -> "Пользователь не найден"
                    405 -> "Метод не разрешён (проверь POST и маршрут)"
                    else -> "HTTP ${resp.code()}"
                }
            }
        } catch (t: Throwable) {
            error = t.message ?: "Network error"
        } finally {
            loading = false
        }
    }

    fun signUp(onSuccess: (UserDto) -> Unit) = viewModelScope.launch {
        error = null
        if (login.isBlank() || password.length < 6) {
            error = "Логин не может быть пустым, а пароль должен быть не менее 6 символов"
            return@launch
        }

        loading = true
        try {
            // Создаем простой DTO с логином и паролем
            val registerDto = RegisterDto(login = login, password = password)

            // Отправляем на сервер
            val response = api.addUser(registerDto)

                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: run { error = "Пустой ответ от сервера" }
                } else {
                // Обрабатываем ошибки, которые возвращает твой API
                error = when (response.code()) {
                    400 -> "Логин и пароль обязательны"
                    409 -> "Пользователь с таким логином уже существует"
                    else -> "Ошибка регистрации: HTTP ${response.code()}"
                }
            }
        } catch (t: Throwable) {
            error = t.message ?: "Ошибка сети"
        } finally {
            loading = false
        }
    }
}