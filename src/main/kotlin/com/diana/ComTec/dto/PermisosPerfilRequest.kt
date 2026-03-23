package com.diana.ComTec.dto

data class PermisosPerfilRequest(
    val idModulo: Int = 0,
    val idPerfil: Int = 0,
    val bitAgregar: Boolean  = false,
    val bitEditar: Boolean   = false,
    val bitConsulta: Boolean = false,
    val bitEliminar: Boolean = false,
    val bitDetalle: Boolean  = false
)