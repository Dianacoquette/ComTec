package com.diana.ComTec.repository

import com.diana.ComTec.model.Modulo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModuloRepository : JpaRepository<Modulo, Int> {
    fun findByStrNombreModuloContainingIgnoreCase(nombre: String, pageable: Pageable): Page<Modulo>
}