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

    fun listar(page: Int, size: Int = 5): Page<PermisosPerfil> =
        permisosPerfilRepository.findAll(PageRequest.of(page, size))

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
}