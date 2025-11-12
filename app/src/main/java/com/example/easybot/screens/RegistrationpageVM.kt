package com.example.easybot.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.UserDto
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
        loading = true
        try {
            val r = api.getUserByLoginAndPassword(login, password)
            if (r.isSuccessful) {
                r.body()?.let(onSuccess) ?: run { error = "Неверный логин/пароль" }
            } else error = "HTTP ${r.code()}"
        } catch (t: Throwable) { error = t.message ?: "Network error" }
        finally { loading = false }
    }

    fun signUp(onSuccess: (UserDto) -> Unit) = viewModelScope.launch {
        if (login.isBlank() || password.length < 6) { error = "Пароль ≥ 6"; return@launch }
        loading = true
        try {
            val r = api.addUser(UserDto(login = login, password = password))
            if (r.isSuccessful) r.body()?.let(onSuccess) ?: run { error = "Пустой ответ" }
            else error = "HTTP ${r.code()}"
        } catch (t: Throwable) { error = t.message ?: "Network error" }
        finally { loading = false }
    }
}