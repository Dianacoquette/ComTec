package com.diana.ComTec.config

import com.diana.ComTec.model.PermisosPerfil

data class PermisoModulo(
    val agregar: Boolean  = false,
    val editar: Boolean   = false,
    val consulta: Boolean = false,
    val eliminar: Boolean = false,
    val detalle: Boolean  = false
)

fun obtenerPermisosModulo(
    permisos: List<PermisosPerfil>,
    nombreModulo: String
): PermisoModulo {
    val permiso = permisos.find { it.modulo.strNombreModulo == nombreModulo }
        ?: return PermisoModulo()
    return PermisoModulo(
        agregar  = permiso.bitAgregar,
        editar   = permiso.bitEditar,
        consulta = permiso.bitConsulta,
        eliminar = permiso.bitEliminar,
        detalle  = permiso.bitDetalle
    )
}