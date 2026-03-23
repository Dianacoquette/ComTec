package com.diana.ComTec.dto

data class PermisosPerfilResponse(
    val id: Int,
    val idModulo: Int,
    val strNombreModulo: String,
    val idPerfil: Int,
    val strNombrePerfil: String,
    val bitAgregar: Boolean,
    val bitEditar: Boolean,
    val bitConsulta: Boolean,
    val bitEliminar: Boolean,
    val bitDetalle: Boolean
)