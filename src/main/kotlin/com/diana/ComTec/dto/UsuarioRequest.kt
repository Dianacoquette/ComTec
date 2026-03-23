package com.diana.ComTec.dto

data class UsuarioRequest(
    val id: Int = 0,
    val strNombreUsuario: String = "",
    val idPerfil: Int = 0,
    val strPwd: String = "",
    val idEstadoUsuario: Int = 1,
    val strCorreo: String? = null,
    val strNumeroCelular: String? = null
)