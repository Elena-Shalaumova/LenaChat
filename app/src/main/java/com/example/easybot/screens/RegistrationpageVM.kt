package com.example.easybot.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easybot.LoginReq
import com.example.easybot.UserDto
import com.example.easybot.WebApiChatAI
import com.example.easybot.provideApi
import kotlinx.coroutines.launch
import com.example.easybot.PasswordHasher

class RegistrationpageVM(private val api: WebApiChatAI = provideApi()
) : ViewModel() {
    var login by mutableStateOf("")
    var password by mutableStateOf("")
    var error by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)

    fun signIn(onSuccess: (UserDto) -> Unit) = viewModelScope.launch {
        error = null
        if (login.isBlank() || password.isBlank()) {
            error = "Введите логин и пароль"; return@launch
        }
        loading = true
        try {
            val resp = api.login(LoginReq(login, password))
            if (resp.isSuccessful) {
                resp.body()?.let(onSuccess) ?: run { error = "Пустой ответ сервера" }
            } else {
                error = when (resp.code()) {
                    401 -> "Неверный логин/пароль"
                    404 -> "Пользователь не найден"
                    405 -> "Метод не разрешён (проверь @POST и маршрут)"
                    else -> "HTTP ${resp.code()}"
                }
            }
        } catch (t: Throwable) {
            error = t.message ?: "Network error"
        } finally { loading = false }
    }

//    fun signUp(onSuccess: (UserDto) -> Unit) = viewModelScope.launch {
//        if (login.isBlank() || password.length < 6) { error = "Пароль ≥ 6"; return@launch }
//        loading = true
//        try {
//            val r = api.addUser(UserDto(login = login, password = password))
//            if (r.isSuccessful) r.body()?.let(onSuccess) ?: run { error = "Пустой ответ" }
//            else error = "HTTP ${r.code()}"
//        } catch (t: Throwable) { error = t.message ?: "Network error" }
//        finally { loading = false }
//    }
fun signUp(onSuccess: (UserDto) -> Unit) = viewModelScope.launch {
    if (login.isBlank() || password.length < 6) {
        error = "Пароль ≥ 6"
        return@launch
    }

    loading = true
    try {
        // 1) превращаем пароль в CharArray
        val pwdChars = password.toCharArray()

        // 2) генерим соль
        val salt = PasswordHasher.generateSalt()

        // 3) считаем PBKDF2-хэш (100_000 итераций)
        val hash = PasswordHasher.hashPassword(
            password = pwdChars,
            salt = salt,
            iterations = PasswordHasher.DEFAULT_ITERATIONS   // либо PasswordHasher.defaultIterations
        )

        // 4) кодируем в base64 для пересылки
        val saltB64 = PasswordHasher.encodeBase64(salt)
        val hashB64 = PasswordHasher.encodeBase64(hash)

        // (опционально) затираем пароль в памяти
        java.util.Arrays.fill(pwdChars, '\u0000')

        // 5) формируем DTO без чистого пароля
        val dto = UserDto(
            login = login,
            password = null,
            password_hash = hashB64,
            salt = saltB64,
            iterations = PasswordHasher.DEFAULT_ITERATIONS,
            is_hashed = true
        )

        // 6) отправляем на сервер
        val r = api.addUser(dto)
        if (r.isSuccessful) {
            r.body()?.let(onSuccess) ?: run { error = "Пустой ответ" }
        } else {
            error = "HTTP ${r.code()}"
        }

    } catch (t: Throwable) {
        error = t.message ?: "Network error"
    } finally {
        loading = false
    }
}

}