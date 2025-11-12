package com.example.easybot

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

//object PasswordHasher {
//    private const val SALT_BYTES = 16
//    private const val HASH_BYTES = 32 // 256 bits
//    private const val DEFAULT_ITERATIONS = 100_000
//
//    fun generateSalt(): ByteArray {
//        val sr = SecureRandom()
//        val salt = ByteArray(SALT_BYTES)
//        sr.nextBytes(salt)
//        return salt
//    }
//
//    fun hashPassword(password: CharArray, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): ByteArray {
//        val spec = PBEKeySpec(password, salt, iterations, HASH_BYTES * 8)
//        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//        val key = skf.generateSecret(spec).encoded
//        spec.clearPassword()
//        return key
//    }
//
//    fun encodeBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
//    fun decodeBase64(str: String): ByteArray = Base64.decode(str, Base64.NO_WRAP)
//
//    // Helper to produce the storage string
//    fun makeStorageString(hash: ByteArray, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): String {
//        val h = encodeBase64(hash)
//        val s = encodeBase64(salt)
//        return "$iterations:$s:$h"
//    }
//
//    // For verification on client if needed
//    fun verify(password: CharArray, salt: ByteArray, iterations: Int, expectedHash: ByteArray): Boolean {
//        val candidate = hashPassword(password, salt, iterations)
//        return candidate.contentEquals(expectedHash)
//    }

/**
 * Утилита для хэширования паролей PBKDF2 (PBKDF2WithHmacSHA256)
 * Работает полностью локально, без внешних библиотек.
 *
 * Используется при регистрации (signUp):
 *  1. Генерация соли
 *  2. Создание хэша пароля
 *  3. Отправка base64-хэша, соли и числа итераций на сервер
 */
object PasswordHasher {

    // --- Константы ---
    const val SALT_BYTES = 16                 // длина соли 16 байт
    const val HASH_BYTES = 32                 // длина хэша 32 байта (256 бит)
    const val DEFAULT_ITERATIONS = 100_000    // количество итераций PBKDF2

    /**
     * Генерация случайной соли
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Хэширование пароля с солью
     *
     * @param password CharArray пароля
     * @param salt соль
     * @param iterations количество итераций (по умолчанию 100_000)
     */
    fun hashPassword(password: CharArray, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): ByteArray {
        val spec = PBEKeySpec(password, salt, iterations, HASH_BYTES * 8)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded
        spec.clearPassword() // очищаем пароль из памяти
        return hash
    }

    /**
     * Кодирование байтов в Base64
     */
    fun encodeBase64(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    /**
     * Декодирование Base64 обратно в байты
     */
    fun decodeBase64(str: String): ByteArray =
        Base64.decode(str, Base64.NO_WRAP)

    /**
     * Упаковка всего в одну строку вида:
     * "100000:BASE64_SALT:BASE64_HASH"
     *
     * Можно хранить как единое поле в базе.
     */
    fun makeStorageString(hash: ByteArray, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): String {
        val h = encodeBase64(hash)
        val s = encodeBase64(salt)
        return "$iterations:$s:$h"
    }

    /**
     * Проверка правильности пароля (если нужно локально)
     */
    fun verify(password: CharArray, salt: ByteArray, iterations: Int, expectedHash: ByteArray): Boolean {
        val candidate = hashPassword(password, salt, iterations)
        return candidate.contentEquals(expectedHash)
    }
}

//}
