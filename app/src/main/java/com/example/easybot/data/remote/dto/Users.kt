package com.example.easybot.data.remote.dto

data class UserDto(
    val id: Int? = null,
    val login: String,
    val password: String? = null,
    val password_hash: String? = null,
    val salt: String? = null,
    val iterations: Int? = null,
    val is_hashed: Boolean? = false,
    val modelChanged: Boolean = false,
    val model: String? = null
)

class Users {
}