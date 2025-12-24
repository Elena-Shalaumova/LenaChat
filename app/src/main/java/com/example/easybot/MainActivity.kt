package com.example.easybot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.easybot.screens.theme.EasyBotTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.easybot.core.session.UserSession
import com.example.easybot.data.local.AuthSession
import com.example.easybot.data.local.AuthStorage
import com.example.easybot.data.local.ThemePreferences
import com.example.easybot.featureauth.ui.MyAppRoot
import com.example.easybot.screens.navigation.Routes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authStorage = AuthStorage(applicationContext)

            setContent {
                val appContext = applicationContext
                val authStorage = remember { AuthStorage(appContext) }
                val systemDarkTheme = isSystemInDarkTheme()
                var isDarkTheme by rememberSaveable {
                    mutableStateOf(ThemePreferences.getTheme(appContext, systemDarkTheme))
                }

                // Читаем единый контекст сессии из DataStore
                val session: AuthSession? =
                    produceState<AuthSession?>(initialValue = null) {
                        authStorage.sessionFlow.collect { value = it }
                    }.value

                // Можно показать Splash/Loader, пока DataStore не отдал значения
                if (session == null) return@setContent

                // Если флаг "авторизован", но userId не сохранён — это битая сессия
                if (session.isAuthorized && session.userId == null) {
                    LaunchedEffect(Unit) { authStorage.clear() }
                    MyAppRoot(startDestination = Routes.Register)
                    return@setContent
                }

                // Восстанавливаем значения в UserSession (так как твои экраны/репозитории на него опираются)
                LaunchedEffect(session.isAuthorized, session.userId, session.login) {
                    if (session.isAuthorized) {
                        UserSession.userId = session.userId!!.toLong()
                        UserSession.login = session.login // может быть null, ок
                    } else {
                        UserSession.userId = null
                        UserSession.login = null
                    }
                }

                val start = if (session.isAuthorized) Routes.ChatList else Routes.Register
                EasyBotTheme(darkTheme = isDarkTheme) {
                    MyAppRoot(
                        startDestination = start,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { enabled ->
                            isDarkTheme = enabled
                            ThemePreferences.saveTheme(appContext, enabled)
                        }
                    )
                }
            }
        }
    }}



