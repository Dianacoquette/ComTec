package com.diana.ComTec.service

import com.diana.ComTec.model.Perfil
import com.diana.ComTec.repository.PerfilRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class PerfilService(private val perfilRepository: PerfilRepository) {

    fun listar(page: Int, size: Int = 5): Page<Perfil> =
        perfilRepository.findAll(PageRequest.of(page, size))

    fun buscar(nombre: String, page: Int, size: Int = 5): Page<Perfil> =
        perfilRepository.findByStrNombrePerfilContainingIgnoreCase(
            nombre, PageRequest.of(page, size)
        )

    fun obtener(id: Int): Perfil? = perfilRepository.findById(id).orElse(null)

    fun guardar(perfil: Perfil): Perfil = perfilRepository.save(perfil)

    fun actualizar(id: Int, perfil: Perfil): Perfil? {
        if (!perfilRepository.existsById(id)) return null
        return perfilRepository.save(perfil.copy(id = id))
    }

    fun eliminar(id: Int): Boolean {
        if (!perfilRepository.existsById(id)) return false
        perfilRepository.deleteById(id)
        return true
    }
}