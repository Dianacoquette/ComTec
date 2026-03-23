package com.diana.ComTec.service

import com.diana.ComTec.model.Modulo
import com.diana.ComTec.repository.ModuloRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class ModuloService(private val moduloRepository: ModuloRepository) {

    fun listar(page: Int, size: Int = 5): Page<Modulo> =
        moduloRepository.findAll(PageRequest.of(page, size))

    fun buscar(nombre: String, page: Int, size: Int = 5): Page<Modulo> =
        moduloRepository.findByStrNombreModuloContainingIgnoreCase(
            nombre, PageRequest.of(page, size)
        )

    fun obtener(id: Int): Modulo? = moduloRepository.findById(id).orElse(null)

    fun guardar(modulo: Modulo): Modulo = moduloRepository.save(modulo)

    fun actualizar(id: Int, modulo: Modulo): Modulo? {
        if (!moduloRepository.existsById(id)) return null
        return moduloRepository.save(modulo.copy(id = id))
    }

    fun eliminar(id: Int): Boolean {
        if (!moduloRepository.existsById(id)) return false
        moduloRepository.deleteById(id)
        return true
    }
}