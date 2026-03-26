package com.diana.ComTec.service

import com.diana.ComTec.dto.PermisosPerfilRequest
import com.diana.ComTec.dto.PermisosPerfilResponse
import com.diana.ComTec.model.PermisosPerfil
import com.diana.ComTec.repository.ModuloRepository
import com.diana.ComTec.repository.PerfilRepository
import com.diana.ComTec.repository.PermisosPerfilRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class PermisosPerfilService(
    private val permisosPerfilRepository: PermisosPerfilRepository,
    private val perfilRepository: PerfilRepository,
    private val moduloRepository: ModuloRepository
) {

    // Listar con filtro opcional por perfil
    fun listar(page: Int, perfilFiltro: Int = 0, size: Int = 5): Page<PermisosPerfil> {
        val pageable = PageRequest.of(page, size)
        return if (perfilFiltro > 0)
            permisosPerfilRepository.findByPerfilId(perfilFiltro, pageable)
        else
            permisosPerfilRepository.findAll(pageable)
    }

    fun obtener(id: Int): PermisosPerfil? =
        permisosPerfilRepository.findById(id).orElse(null)

    fun crear(req: PermisosPerfilRequest): PermisosPerfil {
        val modulo = moduloRepository.findById(req.idModulo)
            .orElseThrow { IllegalArgumentException("Módulo no encontrado") }
        val perfil = perfilRepository.findById(req.idPerfil)
            .orElseThrow { IllegalArgumentException("Perfil no encontrado") }
        return permisosPerfilRepository.save(
            PermisosPerfil(
                modulo      = modulo,
                perfil      = perfil,
                bitAgregar  = req.bitAgregar,
                bitEditar   = req.bitEditar,
                bitConsulta = req.bitConsulta,
                bitEliminar = req.bitEliminar,
                bitDetalle  = req.bitDetalle
            )
        )
    }

    fun actualizar(id: Int, req: PermisosPerfilRequest): PermisosPerfil? {
        val existente = permisosPerfilRepository.findById(id).orElse(null) ?: return null
        val modulo = moduloRepository.findById(req.idModulo)
            .orElseThrow { IllegalArgumentException("Módulo no encontrado") }
        val perfil = perfilRepository.findById(req.idPerfil)
            .orElseThrow { IllegalArgumentException("Perfil no encontrado") }
        return permisosPerfilRepository.save(
            existente.copy(
                modulo      = modulo,
                perfil      = perfil,
                bitAgregar  = req.bitAgregar,
                bitEditar   = req.bitEditar,
                bitConsulta = req.bitConsulta,
                bitEliminar = req.bitEliminar,
                bitDetalle  = req.bitDetalle
            )
        )
    }

    // Toggle individual de un permiso
    fun togglePermiso(id: Int, campo: String, valor: Boolean): PermisosPerfil? {
        val existente = permisosPerfilRepository.findById(id).orElse(null) ?: return null
        val actualizado = when (campo) {
            "bitAgregar"  -> existente.copy(bitAgregar  = valor)
            "bitEditar"   -> existente.copy(bitEditar   = valor)
            "bitConsulta" -> existente.copy(bitConsulta = valor)
            "bitEliminar" -> existente.copy(bitEliminar = valor)
            "bitDetalle"  -> existente.copy(bitDetalle  = valor)
            else          -> return null
        }
        return permisosPerfilRepository.save(actualizado)
    }

    fun eliminar(id: Int): Boolean {
        if (!permisosPerfilRepository.existsById(id)) return false
        permisosPerfilRepository.deleteById(id)
        return true
    }

    fun toResponse(p: PermisosPerfil) = PermisosPerfilResponse(
        id              = p.id,
        idModulo        = p.modulo.id,
        strNombreModulo = p.modulo.strNombreModulo,
        idPerfil        = p.perfil.id,
        strNombrePerfil = p.perfil.strNombrePerfil,
        bitAgregar      = p.bitAgregar,
        bitEditar       = p.bitEditar,
        bitConsulta     = p.bitConsulta,
        bitEliminar     = p.bitEliminar,
        bitDetalle      = p.bitDetalle
    )

    fun listarModulos() = moduloRepository.findAll()
        .map { mapOf("id" to it.id, "nombre" to it.strNombreModulo) }

    fun listarPerfiles() = perfilRepository.findAll()
        .map { mapOf("id" to it.id, "nombre" to it.strNombrePerfil) }

    fun listarModulosConPermisos(perfilFiltro: Int, page: Int, size: Int = 5): Map<String, Any> {
        val todosLosModulos = moduloRepository.findAll()
        val permisosExistentes = if (perfilFiltro > 0)
            permisosPerfilRepository.findByPerfilId(perfilFiltro)
        else
            emptyList()

        // Cruzar módulos con permisos existentes
        val filas = todosLosModulos.map { modulo ->
            val permiso = permisosExistentes.find { it.modulo.id == modulo.id }
            mapOf(
                "idModulo"        to modulo.id,
                "strNombreModulo" to modulo.strNombreModulo,
                "idPermiso"       to (permiso?.id ?: 0),
                "idPerfil"        to perfilFiltro,        // ← este faltaba
                "bitAgregar"      to (permiso?.bitAgregar  ?: false),
                "bitEditar"       to (permiso?.bitEditar   ?: false),
                "bitConsulta"     to (permiso?.bitConsulta ?: false),
                "bitEliminar"     to (permiso?.bitEliminar ?: false),
                "bitDetalle"      to (permiso?.bitDetalle  ?: false),
                "tienePermiso"    to (permiso != null)
            )
        }

        // Paginación manual
        val totalElements = filas.size
        val totalPages    = if (totalElements == 0) 1 else (totalElements + size - 1) / size
        val fromIndex     = page * size
        val toIndex       = minOf(fromIndex + size, totalElements)
        val content       = if (fromIndex >= totalElements) emptyList() else filas.subList(fromIndex, toIndex)

        return mapOf(
            "content"       to content,
            "totalPages"    to totalPages,
            "totalElements" to totalElements,
            "currentPage"   to page
        )
    }

    // Crear permiso desde cero (cuando el registro no existe)
    fun crearDesdeToggle(idPerfil: Int, idModulo: Int, campo: String, valor: Boolean): PermisosPerfil? {
        val perfil = perfilRepository.findById(idPerfil).orElse(null) ?: return null
        val modulo = moduloRepository.findById(idModulo).orElse(null) ?: return null
        val nuevo = PermisosPerfil(
            perfil      = perfil,
            modulo      = modulo,
            bitAgregar  = campo == "bitAgregar"  && valor,
            bitEditar   = campo == "bitEditar"   && valor,
            bitConsulta = campo == "bitConsulta" && valor,
            bitEliminar = campo == "bitEliminar" && valor,
            bitDetalle  = campo == "bitDetalle"  && valor
        )
        return permisosPerfilRepository.save(nuevo)
    }
}