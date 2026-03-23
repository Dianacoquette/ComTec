package com.diana.ComTec.dto

data class UsuarioResponse(
    val id: Int,
    val strNombreUsuario: String,
    val idPerfil: Int,
    val strNombrePerfil: String,
    val idEstadoUsuario: Int,
    val strEstado: String,
    val strCorreo: String?,
    val strNumeroCelular: String?,
    val strImagen: String?
)