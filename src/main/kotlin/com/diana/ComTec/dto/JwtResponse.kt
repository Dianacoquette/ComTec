package com.diana.ComTec.dto

data class JwtResponse(
    val token: String,
    val username: String,
    val perfil: String
)