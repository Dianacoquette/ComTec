package com.diana.ComTec.repository

import com.diana.ComTec.model.Perfil
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PerfilRepository : JpaRepository<Perfil, Int> {
    fun findByStrNombrePerfilContainingIgnoreCase(nombre: String, pageable: Pageable): Page<Perfil>
}