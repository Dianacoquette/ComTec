package com.diana.ComTec.dto

data class LoginRequest(
    val username: String = "",
    val password: String = "",
    val captchaToken: String = ""
)