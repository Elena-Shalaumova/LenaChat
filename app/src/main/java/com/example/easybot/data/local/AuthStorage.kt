package com.example.easybot.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_storage")

data class AuthSession(
    val isAuthorized: Boolean,
    val userId: Int?,
    val login: String?,
    val token: String?
)

class AuthStorage(private val context: Context) {

    private object Keys {
        val IS_AUTHORIZED = booleanPreferencesKey("is_authorized")
        val USER_ID = intPreferencesKey("user_id")
        val TOKEN = stringPreferencesKey("token")
        val LOGIN = stringPreferencesKey("login")
    }

    // Твои текущие флоу оставляем (они полезны)
    val isAuthorizedFlow: Flow<Boolean> =
        context.authDataStore.data.map { prefs -> prefs[Keys.IS_AUTHORIZED] ?: false }

    val userIdFlow: Flow<Int?> =
        context.authDataStore.data.map { prefs -> prefs[Keys.USER_ID] }

    val tokenFlow: Flow<String?> =
        context.authDataStore.data.map { prefs -> prefs[Keys.TOKEN] }

    val loginFlow: Flow<String?> =
        context.authDataStore.data.map { prefs -> prefs[Keys.LOGIN] }

    // ВАЖНО: единый "снимок" сессии, чтобы MainActivity/экраны могли восстановить контекст
    val sessionFlow: Flow<AuthSession> =
        context.authDataStore.data.map { prefs ->
            AuthSession(
                isAuthorized = prefs[Keys.IS_AUTHORIZED] ?: false,
                userId = prefs[Keys.USER_ID],
                login = prefs[Keys.LOGIN],
                token = prefs[Keys.TOKEN]
            )
        }

    suspend fun saveAuth(
        userId: Int,
        token: String? = null,
        login: String? = null
    ) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.IS_AUTHORIZED] = true
            prefs[Keys.USER_ID] = userId

            if (token != null) prefs[Keys.TOKEN] = token else prefs.remove(Keys.TOKEN)
            if (login != null) prefs[Keys.LOGIN] = login else prefs.remove(Keys.LOGIN)
        }
    }

    // Иногда полезно отдельно обновлять логин/токен без перезаписи userId
    suspend fun updateLogin(login: String?) {
        context.authDataStore.edit { prefs ->
            if (login.isNullOrBlank()) prefs.remove(Keys.LOGIN) else prefs[Keys.LOGIN] = login
        }
    }

    suspend fun updateToken(token: String?) {
        context.authDataStore.edit { prefs ->
            if (token.isNullOrBlank()) prefs.remove(Keys.TOKEN) else prefs[Keys.TOKEN] = token
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { prefs -> prefs.clear() }
    }
}

